package com.epam.businessdictionary;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.epam.businessdictionary",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories =
            noClasses()
                    .that().resideInAPackage("..api..")
                    .should().accessClassesThat().resideInAPackage("..persistence..");

    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..api..");

    @ArchTest
    static final ArchRule repositories_should_not_depend_on_services_or_controllers =
            noClasses()
                    .that().resideInAPackage("..persistence..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application..", "..api..");
}
