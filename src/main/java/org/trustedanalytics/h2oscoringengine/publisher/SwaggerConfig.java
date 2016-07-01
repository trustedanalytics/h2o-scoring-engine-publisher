/**
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.trustedanalytics.h2oscoringengine.publisher;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    Docket userManagementApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(Authentication.class)
                .apiInfo(apiInfo())
                .select()
                .paths(scoringEnginePaths())
                .build()
                .useDefaultResponseMessages(false);
    }

    private Predicate<String> scoringEnginePaths() {
        return or(
                regex("/rest/.*"));
    }

    private String getVersion() {
        String version = System.getenv("VERSION");
        if (version == null || version.length() == 0) {
            version = "dev";
        }
        return version;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("H2O Scoring Engine Publisher API")
                .description("Api endpoints for building H2O scoring engine and exposing it as a JAR file " +
                        "for downloading or publishing it as a service offering in CloudFoundry marketplace")
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/trustedanalytics/h2o-scoring-engine-publisher/blob/master/LICENSE.txt")
                .version(getVersion())
                .build();
    }
}