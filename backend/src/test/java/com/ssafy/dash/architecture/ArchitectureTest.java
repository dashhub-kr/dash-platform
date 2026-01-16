package com.ssafy.dash.architecture;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("아키텍처 규칙 테스트")
@Disabled("코드베이스 리팩토링 후 활성화 예정 - 현재 레이어 위반 다수 존재")
class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.ssafy.dash";
    private static final String PRESENTATION = "..presentation..";
    private static final String PRESENTATION_DTO = "..presentation..dto..";
    private static final String PRESENTATION_REQUEST_DTO = "..presentation..dto..request..";
    private static final String PRESENTATION_RESPONSE_DTO = "..presentation..dto..response..";
    private static final String APPLICATION = "..application..";
    private static final String APPLICATION_DTO = "..application..dto..";
    private static final String DOMAIN = "..domain..";
    private static final String INFRASTRUCTURE = "..infrastructure..";
    private static final String PERSISTENCE = "..persistence..";
    private static final String MAPPER = "..mapper..";

    private static final String[] PRESENTATION_FAMILY = { PRESENTATION, PRESENTATION_DTO };
    private static final String[] APPLICATION_AND_PRESENTATION_FAMILY = {
            APPLICATION, APPLICATION_DTO, PRESENTATION, PRESENTATION_DTO
    };

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

    @Test
    @DisplayName("계층형 아키텍처 의존성 규칙 준수 (Presentation -> Application -> Domain <- Infrastructure)")
    void layeredArchitectureShouldBeRespected() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Presentation").definedBy(PRESENTATION)
                .layer("Application").definedBy(APPLICATION, APPLICATION_DTO)
                .layer("Domain").definedBy(DOMAIN)
                .layer("Infrastructure").definedBy(INFRASTRUCTURE, PERSISTENCE, MAPPER)
                .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Presentation")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain 계층은 Infrastructure 계층을 의존해서는 안 된다 (DIP 준수)")
    void domainShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
                .because("도메인은 영속성 구현체에 절대 의존하지 않는다")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain 계층은 웹 관련 패키지(Spring Web 등)를 의존해서는 안 된다")
    void domainShouldNotDependOnWebLayer() {
        noClasses()
                .that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat().resideInAPackage("org.springframework.web..")
                .orShould().dependOnClassesThat().resideInAPackage("javax.servlet..")
                .orShould().dependOnClassesThat().resideInAPackage("jakarta.servlet..")
                .because("도메인 모델은 웹 요청/응답 기술에 의존하지 않는다")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain 계층은 DTO나 상위 계층을 의존해서는 안 된다")
    void domainShouldStayIndependentFromDtoAndUpperLayers() {
        noClasses()
                .that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat()
                .resideInAnyPackage(APPLICATION, APPLICATION_DTO, PRESENTATION, PRESENTATION_DTO)
                .because("DDD Aggregate는 Application/Presentation DTO에 결코 의존하지 않는다")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Service(Application) 계층은 Controller/Presentation 패키지를 의존해서는 안 된다")
    void applicationShouldNotDependOnPresentation() {
        noClasses()
                .that().resideInAPackage(APPLICATION)
                .or().resideInAPackage(APPLICATION_DTO)
                .should().dependOnClassesThat().resideInAPackage(PRESENTATION)
                .because("UseCase 계층은 상위 어댑터에 역의존하지 않는다")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Controller는 Service나 UseCase만 호출해야 하며, Repository를 직접 호출해서는 안 된다")
    void controllerShouldNotAccessRepositoryDirectly() {
        noClasses()
                .that().resideInAPackage(PRESENTATION)
                .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
                .orShould().dependOnClassesThat().resideInAPackage(PERSISTENCE)
                .orShould().dependOnClassesThat().resideInAPackage(MAPPER)
                .because("프레젠테이션 계층은 Persistence 세부 구현을 직접 호출하지 않는다")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Presentation Request DTO는 Presentation 계층에서만 사용된다")
    void presentationRequestDtosShouldNotLeak() {
        classes()
                .that().resideInAPackage(PRESENTATION_REQUEST_DTO)
                .should().onlyHaveDependentClassesThat().resideInAnyPackage(PRESENTATION_FAMILY)
                .because("요청 DTO는 Controller 계층 외부로 노출되면 안 된다")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Presentation Response DTO는 Presentation 계층에서만 사용된다")
    void presentationResponseDtosShouldNotLeak() {
        classes()
                .that().resideInAPackage(PRESENTATION_RESPONSE_DTO)
                .should().onlyHaveDependentClassesThat().resideInAnyPackage(PRESENTATION_FAMILY)
                .because("응답 DTO는 외부 어댑터에서만 소비되어야 한다")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Application DTO는 Application/Presentation 계층에서만 사용된다")
    void applicationDtosShouldOnlyBeConsumedByAllowedLayers() {
        classes()
                .that().resideInAPackage(APPLICATION_DTO)
                .should().onlyHaveDependentClassesThat().resideInAnyPackage(APPLICATION_AND_PRESENTATION_FAMILY)
                .because("Command/Result DTO는 인바운드 어댑터나 UseCase에서만 사용된다")
                .check(importedClasses);
    }

}
