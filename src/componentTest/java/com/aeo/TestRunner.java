package com.aeo;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/componentTest/resources/features",
        glue =
        { "com.aeo.salad.stepDefinitions", "com.aeo.salad.helpers", "com.aeo.salad.cucumber", "com.aeo.step_defs", "com.aeo.hooks" },
        plugin =
        { "pretty", "html:results/html/cucumber.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" },
        tags = "@runAll")
public class TestRunner {}
