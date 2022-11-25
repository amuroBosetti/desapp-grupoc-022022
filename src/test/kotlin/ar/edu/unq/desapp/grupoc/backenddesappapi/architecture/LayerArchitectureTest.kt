package ar.edu.unq.desapp.grupoc.backenddesappapi.architecture

import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers.HttpController
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Controller

class LayerArchitectureTest {

    @Test
    fun `a class in the service package cannot call a class in the webservice package`() {
        val rule = classes().that().resideInAPackage("..controllers..")
            .should().onlyBeAccessed().byClassesThat().resideOutsideOfPackage("..service..")

        rule.check(classesToTest())
    }

    @Test
    fun `a class that ends with controller has to be in the controllers package`() {
        val rule = classes().that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers")

        rule.check(classesToTest())
    }


    @Test
    fun `a class annotated with @Controller has to inherit from HttpController class`() {
        val rule = classes().that().areAnnotatedWith(Controller::class.java)
            .should().beAssignableTo(HttpController::class.java)

        rule.check(classesToTest())
    }

    private fun classesToTest(): JavaClasses {
        return ClassFileImporter().withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("ar.edu.unq.desapp.grupoc.backenddesappapi")
    }
}