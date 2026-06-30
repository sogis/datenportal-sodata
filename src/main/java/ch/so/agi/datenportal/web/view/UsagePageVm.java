package ch.so.agi.datenportal.web.view;

import java.util.List;

public record UsagePageVm(
        PageChromeVm chrome,
        String title,
        List<DirectAccessRowVm> directAccessRows,
        List<CodeExampleVm> codeExamples,
        List<StarterRecipeVm> starterRecipes) {

    public UsagePageVm {
        directAccessRows = List.copyOf(directAccessRows);
        codeExamples = List.copyOf(codeExamples);
        starterRecipes = List.copyOf(starterRecipes);
    }
}
