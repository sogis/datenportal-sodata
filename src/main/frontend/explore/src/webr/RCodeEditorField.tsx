export function RCodeEditorField({
  value,
  onChange,
  onRun,
  disabled
}: {
  value: string;
  onChange: (value: string) => void;
  onRun: () => void;
  disabled: boolean;
}) {
  return (
    <div className="dp-explore-editor dp-explore-r-editor">
      <textarea
        className="dp-explore-editor__textarea dp-explore-r-editor__textarea"
        aria-label="R bearbeiten"
        value={value}
        disabled={disabled}
        spellCheck={false}
        onChange={(event) => onChange(event.target.value)}
        onKeyDown={(event) => {
          if ((event.metaKey || event.ctrlKey) && event.key === 'Enter') {
            event.preventDefault();
            onRun();
          }
        }}
      />
    </div>
  );
}
