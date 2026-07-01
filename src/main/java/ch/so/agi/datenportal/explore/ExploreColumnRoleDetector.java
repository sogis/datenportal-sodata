package ch.so.agi.datenportal.explore;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public final class ExploreColumnRoleDetector {

    public Set<ExploreColumnRole> detectRoles(ExploreColumnSource column) {
        var roles = new LinkedHashSet<ExploreColumnRole>();
        String name = column.name();
        String type = column.type();

        if (isIdentifier(name, type)) {
            roles.add(ExploreColumnRole.IDENTIFIER);
        }
        if (isLabel(name, type)) {
            roles.add(ExploreColumnRole.LABEL);
        }
        if (isText(type) && !isIdentifier(name, type)) {
            roles.add(ExploreColumnRole.CATEGORY);
        }
        if (isMeasure(name, type)) {
            roles.add(ExploreColumnRole.MEASURE);
        }
        if (isDateLike(name, type)) {
            roles.add(ExploreColumnRole.DATE);
        }
        if (isYearLike(name, type)) {
            roles.add(ExploreColumnRole.YEAR);
        }
        if (isGeometry(name, type)) {
            roles.add(ExploreColumnRole.GEOMETRY);
        }
        if (isMunicipality(name)) {
            roles.add(ExploreColumnRole.MUNICIPALITY);
        }
        if (roles.isEmpty()) {
            roles.add(ExploreColumnRole.UNKNOWN);
        }
        return Set.copyOf(roles);
    }

    boolean isIdentifier(String name, String type) {
        String normalized = normalize(name);
        return normalized.equals("id")
                || normalized.endsWith("_id")
                || normalized.equals("uuid")
                || normalized.equals("oid")
                || normalized.equals("t_id")
                || normalized.equals("basket")
                || normalized.equals("bfs_nr")
                || normalized.equals("egid")
                || normalized.equals("ewid");
    }

    boolean isLabel(String name, String type) {
        String normalized = normalize(name);
        return isText(type)
                && (normalized.equals("name")
                || normalized.equals("titel")
                || normalized.equals("title")
                || normalized.equals("bezeichnung")
                || normalized.equals("gemeindename"));
    }

    boolean isMunicipality(String name) {
        String normalized = normalize(name);
        return normalized.equals("bfs_nr")
                || normalized.equals("bfsnr")
                || normalized.equals("gemeinde")
                || normalized.equals("gemeindename")
                || normalized.equals("gemeinde_name")
                || normalized.equals("municipality");
    }

    boolean isDateLike(String name, String type) {
        String normalized = normalize(name);
        String normalizedType = normalize(type);
        return normalizedType.contains("date")
                || normalizedType.contains("time")
                || normalized.equals("datum")
                || normalized.equals("date")
                || normalized.equals("stand")
                || normalized.equals("stichtag")
                || normalized.equals("gueltig_ab")
                || normalized.equals("gueltig_bis")
                || normalized.equals("updated_at");
    }

    boolean isYearLike(String name, String type) {
        String normalized = normalize(name);
        return normalized.equals("jahr")
                || normalized.equals("year")
                || normalized.equals("periode")
                || normalized.equals("berichtsjahr");
    }

    boolean isMeasure(String name, String type) {
        return isNumeric(type) && !isIdentifier(name, type) && !isYearLike(name, type);
    }

    boolean isGeometry(String name, String type) {
        String normalized = normalize(name);
        String normalizedType = normalize(type);
        return normalized.equals("geom")
                || normalized.equals("geometry")
                || normalized.equals("wkb_geometry")
                || normalized.equals("the_geom")
                || normalized.equals("wkt")
                || normalizedType.contains("geometry");
    }

    private static boolean isText(String type) {
        String normalized = normalize(type);
        return normalized.contains("char")
                || normalized.contains("text")
                || normalized.contains("string")
                || normalized.contains("varchar");
    }

    private static boolean isNumeric(String type) {
        String normalized = normalize(type);
        return normalized.contains("int")
                || normalized.contains("double")
                || normalized.contains("float")
                || normalized.contains("decimal")
                || normalized.contains("numeric")
                || normalized.contains("number")
                || normalized.contains("real");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
