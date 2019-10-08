package com.youxin.app.config.swagger;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.youxin.app.utils.Result;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
public class SwaggerConfig {


    @Bean
    public Docket createRestApi() {
    	  //添加head参数配置start
//        ParameterBuilder tokenPar = new ParameterBuilder();
//        List<Parameter> pars = new ArrayList<>();
//        tokenPar.name("access_token").description("令牌").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
//        pars.add(tokenPar.build());
//    	 List<ResponseMessage> responseMessageList = new ArrayList<>();
//    	    responseMessageList.add(new ResponseMessageBuilder().code(404).message("找不到资源").responseModel(new ModelRef("Result")).build());
//    	    responseMessageList.add(new ResponseMessageBuilder().code(409).message("业务逻辑异常").responseModel(new ModelRef("Result")).build());
//    	    responseMessageList.add(new ResponseMessageBuilder().code(422).message("参数校验异常").responseModel(new ModelRef("Result")).build());
//    	    responseMessageList.add(new ResponseMessageBuilder().code(500).message("服务器内部错误").responseModel(new ModelRef("Result")).build());
//    	    responseMessageList.add(new ResponseMessageBuilder().code(503).message("Hystrix异常").responseModel(new ModelRef("Result")).build());

    	
        return new Docket(DocumentationType.SWAGGER_2)
        		
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.youxin.app.controller"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
//                .paths(PathSelectors.regex("^(?!auth).*$"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
//                .globalOperationParameters(pars)
                ;//注意这里;
    }
    
    private List<ApiKey> securitySchemes() {
    	ApiKey ak=new ApiKey("Authorization", "access_token", "header");
    	List<ApiKey> aks=new ArrayList<>();
    	aks.add(ak);
        return aks;
 }
    
    private List<SecurityContext> securityContexts() {
    	 SecurityContext build = SecurityContext.builder()
         .securityReferences(defaultAuth())
//         .forPaths(PathSelectors.regex("^(?!auth).*$"))
         .forPaths(PathSelectors.any())
         .build();
    	 List<SecurityContext> list=new ArrayList<>();
    	 list.add(build);
        return list;
    }
    
    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        SecurityReference securityReference = new SecurityReference("Authorization", authorizationScopes);
        ArrayList<SecurityReference> arrayList = new ArrayList<>();
        arrayList.add(securityReference);
        return arrayList;
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("友信api")
                .description("友信相关接口文档")
                .contact(new Contact("youxin", "http://www.youxinapp.cn", ""))
                .version("2.0")
                .build();
    }



    @Bean
    UiConfiguration uiConfig() {
        return new UiConfiguration(null, "list", "alpha", "schema",
                UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS, false, true, 60000L);
    }
}