#!/usr/bin/env python3
"""Test the built runtime image, including native Explore JSON serialization.

Requires only Python 3 and Docker. Uses bundled fixtures and owns all containers
it creates; no source mounts, external data services or fixed host ports.
"""
import argparse
from html.parser import HTMLParser
from http.client import HTTPException
import json
import re
import subprocess
import sys
import time
import urllib.error
import urllib.request
import uuid


CASES = (
    ('/datasets/ch.so.bauinventar', 'ch.so.bauinventar'),
    ('/series/ch.so.abstimmungsresultate/issues/current', 'ch.so.abstimmungsresultate_2026'),
    ('/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025',
     'ch.so.abstimmungsresultate_2025'),
)
COLUMN_ROLES = {'identifier', 'label', 'category', 'measure', 'date', 'year', 'geometry', 'municipality', 'unknown'}
RECIPE_CATEGORIES = {'preview', 'profile', 'quality', 'category', 'numeric', 'time', 'custom'}
CHART_TYPES = {'bar', 'line', 'scatter', 'histogram', 'pie', 'donut'}


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def docker(*args):
    result = subprocess.run(['docker', *args], capture_output=True, text=True, timeout=60)
    if result.returncode:
        raise RuntimeError(f'docker {args[0]} failed: {result.stderr.strip()}')
    return result.stdout.strip()


class EmbeddedContextParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.contexts = []
        self.current = None

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == 'script' and attrs.get('id') == 'datenportal-explore-context':
            require(attrs.get('type') == 'application/json', 'Embedded context must use application/json')
            self.current = []

    def handle_data(self, data):
        if self.current is not None:
            self.current.append(data)

    def handle_endtag(self, tag):
        if tag == 'script' and self.current is not None:
            self.contexts.append(json.loads(''.join(self.current)))
            self.current = None


def get(base, path, status=200, content_type=None):
    url = base + path
    try:
        response = urllib.request.urlopen(url, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    except (OSError, HTTPException) as error:
        raise AssertionError(f'GET {url}: {error}') from error
    with response:
        require(response.status == status, f'GET {url}: expected HTTP {status}, got {response.status}')
        if content_type:
            actual = response.headers.get_content_type()
            require(actual == content_type, f'GET {url}: expected {content_type}, got {actual}')
        return response.read()


def verify_context(context, canonical, dataset_id):
    require(context['version'] == 5, 'Unexpected context version')
    require(context['datasetId'] == dataset_id, 'Wrong dataset/issue selected')
    require(context['canonicalUrl'] == canonical, 'Wrong canonical URL')
    require(bool(context['title']), 'Missing title')
    execution = context['execution']
    require(execution['engine'] == 'duckdb-wasm' and execution['mode'] == 'browser-local', 'Wrong execution mode')
    for key in ('maxPreviewRows', 'maxResultRows', 'queryTimeoutMs'):
        require(execution[key] > 0, f'Missing execution limit: {key}')
    database = context['catalogDatabase']
    require(re.fullmatch(r'/catalog/catalog\.duckdb\?v=[0-9a-f]{64}', database['url']), 'Invalid DuckDB URL/hash')
    require(database['database'] == 'catalog' and database['schema'] == 'opendata', 'Wrong DuckDB binding')
    require(context['chartsEnabled'] is True and context['webREnabled'] is True, 'Missing Explore capabilities')
    laboratory = context['rLaboratory']
    require(laboratory['dataFrameName'] == 'daten', 'Wrong R data frame')
    require(laboratory['runtimeBaseUrl'] == '/webr/0.6.0/', 'Wrong WebR runtime')
    require(laboratory['packageRepoUrl'] == '/webr-packages/' and 'ggplot2' in laboratory['packages'], 'Missing R packages')
    require('sf' in laboratory['geometryPackages'], 'Missing R geometry packages')
    require(0 < laboratory['recommendedRows'] <= laboratory['warningRows'] <= laboratory['hardRows'], 'Wrong R limits')
    require(laboratory['plotWidth'] > 0 and laboratory['plotHeight'] > 0, 'Missing R plot dimensions')
    map_config = context['map']
    require(map_config['crs'] == 'EPSG:2056', 'Wrong map CRS')
    require(map_config['layer'] == 'ch.so.agi.hintergrundkarte_sw', 'Wrong WMTS layer')
    require(map_config['wmtsUrl'] == (
        'https://geo.so.ch/api/wmts/1.0.0/ch.so.agi.hintergrundkarte_sw/'
        'default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png'
    ), 'Wrong WMTS URL template')
    require(bool(map_config['attribution']), 'Missing map attribution')
    for key in ('maxFeatures', 'maxBytes', 'maxCoordinates'):
        require(map_config[key] > 0, f'Missing map limit: {key}')
    require(bool(context['tables']) and bool(context['recipes']), 'Empty Explore tables/recipes')
    table_ids = set()
    column_count = 0
    for table in context['tables']:
        table_ids.add(table['id'])
        require(table['name'] and table['title'], 'Missing table metadata')
        require(table['parquetUrl'].endswith(dataset_id + '.parquet'), 'Wrong Parquet distribution')
        require(isinstance(table['primary'], bool), 'Missing primary flag')
        for column in table['columns']:
            column_count += 1
            require(column['name'] and column['type'], 'Missing column metadata')
            require(column['roles'] and set(column['roles']) <= COLUMN_ROLES, 'Invalid column role JSON values')
    chart_count = 0
    for recipe in context['recipes']:
        require(recipe['id'] and recipe['title'] and recipe['sql'], 'Missing recipe metadata')
        require(recipe['tableId'] in table_ids, 'Recipe refers to unknown table')
        require(recipe['category'] in RECIPE_CATEGORIES, 'Invalid recipe category JSON value')
        if 'preferredChart' in recipe:
            chart = recipe['preferredChart']
            require(chart['type'] in CHART_TYPES, 'Invalid chart type JSON value')
            require(chart.get('x') and chart.get('y'), 'Missing chart axes')
            chart_count += 1
    return column_count, chart_count


def verify_http(base):
    get(base, '/', content_type='text/html')
    duckdb = get(base, '/catalog/catalog.duckdb')
    require(duckdb[8:12] == b'DUCK', 'Catalog download is not a DuckDB file')
    columns = charts = 0
    for canonical, dataset_id in CASES:
        path = canonical + '/explore'
        try:
            html = get(base, path, content_type='text/html').decode('utf-8')
            parser = EmbeddedContextParser()
            parser.feed(html)
            require(len(parser.contexts) == 1, 'Expected exactly one embedded JSON context')
            context = json.loads(get(base, path + '/context.json', content_type='application/json'))
            require(parser.contexts[0] == context, 'HTML and JSON endpoint contexts differ')
            counts = verify_context(context, canonical, dataset_id)
            columns += counts[0]
            charts += counts[1]
            get(base, context['catalogDatabase']['url'])
        except (AssertionError, KeyError, TypeError, ValueError) as error:
            raise AssertionError(f'Explore regression at {base}{path}: {error}') from error
        print(f'PASS: {path} and context.json', flush=True)
    require(columns > 0, 'Fixtures did not exercise column serialization')
    require(charts > 0, 'Fixtures did not exercise nested Optional<ExploreChartConfigDto> serialization')
    for suffix in ('/explore', '/explore/context.json'):
        get(base, '/datasets/does-not-exist' + suffix, status=404)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--image', required=True)
    parser.add_argument('--runtime', choices=('native', 'jvm'), required=True)
    args = parser.parse_args()
    name = 'sodata-' + args.runtime + '-test-' + uuid.uuid4().hex[:12]
    created = False
    try:
        image = json.loads(docker('image', 'inspect', args.image))[0]
        expected = ['/opt/datenportal/app'] if args.runtime == 'native' else ['java', '-jar', '/opt/datenportal/app.jar']
        require(image['Config']['Entrypoint'] == expected, f'Image does not use the expected {args.runtime} entrypoint: {expected}')
        docker('create', '--name', name, '--user', '1001230000:0', '--publish', '127.0.0.1::8080',
               '--env', 'DATENPORTAL_CATALOG_SOURCE_TYPE=classpath',
               '--env', 'DATENPORTAL_CATALOG_CLASSPATH_LOCATION=published_catalog_full_62_entries.xtf',
               '--env', 'DATENPORTAL_CATALOG_DOWNLOAD_URL=http://localhost:8081/ch.so.datenportal/downloads',
               '--env', 'DATENPORTAL_CATALOG_DUCKDB_SOURCE_TYPE=classpath',
               '--env', 'DATENPORTAL_CATALOG_DUCKDB_CLASSPATH_LOCATION=catalog.duckdb', args.image)
        created = True
        docker('start', name)
        port = json.loads(docker('inspect', name))[0]['NetworkSettings']['Ports']['8080/tcp'][0]['HostPort']
        base = 'http://127.0.0.1:' + port
        deadline = time.monotonic() + 120
        last_error = ''
        while True:
            try:
                health = json.loads(get(base, '/actuator/health/liveness'))
                require(health['status'] == 'UP', 'Liveness is not UP')
                break
            except (AssertionError, ValueError) as error:
                last_error = str(error)
                state = json.loads(docker('inspect', name))[0]['State']
                require(state['Running'], f'Container exited before readiness: {last_error}')
                require(time.monotonic() < deadline, f'Liveness timeout: {last_error}')
                time.sleep(1)
        require(docker('exec', name, 'id', '-u') == '1001230000', 'Unexpected UID')
        verify_http(base)
        print(f'PASS: {args.runtime} image {args.image}, arbitrary UID, health, catalog and Explore', flush=True)
        return 0
    except (AssertionError, RuntimeError, subprocess.SubprocessError, KeyError, ValueError, OSError, HTTPException) as error:
        print(f'FAIL: {args.runtime} image {args.image}: {error}', file=sys.stderr)
        if created:
            subprocess.run(['docker', 'logs', '--tail', '200', name], timeout=30, check=False)
        return 1
    finally:
        if created:
            docker('rm', '--force', name)


if __name__ == '__main__':
    sys.exit(main())
