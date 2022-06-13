package com.ohh.fileServer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * swagger配置类
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        //DocumentationType.SWAGGER_2固定的，代表swagger2
        return new Docket(DocumentationType.SWAGGER_2)
                //.groupName("文件上传下载")//如果配置多个文档的时候，那么需要配置groupName来分组标识
                .apiInfo(apiInfo())//用于生成api信息
                .select()//select()函数返回一个ApiSelectorBuilder实例，用来控制接口swagger做成文档
                .apis(RequestHandlerSelectors.basePackage("com.ohh.fileServer.controller"))//用于指定扫描哪个包下的接口
                .paths(PathSelectors.any())//选择所有的api，如果你想只为部分api生成文档，可以配置这里
                .build();
    }

    /**
     * 用于定义api主界面的信息，比如可以声明所有的api的总标题，描述，版本
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("基于Swagger构建的Rest API文档")//可以用来自定义api的主标题
                .description("XX项目SwaggerAPI管理")//可以用来描述整体的api
                .termsOfServiceUrl("")//用于定义服务的域名
                .version("1.0")//可以用来定义版本
                .build();
    }
}
