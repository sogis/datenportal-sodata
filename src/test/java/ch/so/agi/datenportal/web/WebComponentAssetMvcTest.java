package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebComponentAssetMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesVendoredWebComponentEntrypoint() throws Exception {
        mockMvc.perform(get("/vendor/so-web-components/0.1.10/index.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("customElements.define")))
                .andExpect(content().string(containsString("so-header")));
    }

    @Test
    void servesStylesWithoutLocalPaths() throws Exception {
        assertNoLocalPaths("/vendor/so-web-components/0.1.10/styles/reset.css");
        assertNoLocalPaths("/vendor/so-web-components/0.1.10/styles/fonts.css");
        assertNoLocalPaths("/vendor/so-web-components/0.1.10/styles/tokens.css");
    }

    private void assertNoLocalPaths(String path) throws Exception {
        var result = mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("/Users/")
                .doesNotContain("file:")
                .doesNotContain("/assets/fonts/")
                .doesNotContainPattern("[A-Za-z]:\\\\");
    }
}
