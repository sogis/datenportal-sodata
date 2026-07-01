export async function copyTextToClipboard(
  text: string,
  clipboard: Pick<Clipboard, 'writeText'> | undefined = globalThis.navigator.clipboard
): Promise<boolean> {
  if (!clipboard) {
    return false;
  }
  await clipboard.writeText(text);
  return true;
}

