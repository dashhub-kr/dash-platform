package com.ssafy.dash.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

@DisplayName("아키텍처 규칙 테스트")
class ArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ssafy.dash.board", "com.ssafy.dash.github", "com.ssafy.dash.algorithm");

    @Test
    @DisplayName("계층형 아키텍처 의존성 규칙 준수 (Presentation -> Application -> Domain <- Infrastructure)")
    void layeredArchitectureShouldBeRespected() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()

                // --- 4계층 레이어 정의 ---
                .layer("Presentation").definedBy("..presentation..", "..controller..")
                // ┌ DTO는 프레젠테이션 계층에 속한다고 간주 (통합 관리)
                .layer("Application").definedBy("..application..", "..service..", "..dto..")
                .layer("Domain").definedBy("..domain..")
                .layer("Infrastructure").definedBy("..infrastructure..", "..mapper..")

                // --- 의존성 규칙 정의 ---
                .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation")
                // ┌ Presentation은 DTO 변환이나 응답 생성을 위해 Domain 계층을 참조하는 것을 임시 허용 (실용성 고려)
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Presentation")
                // ┌ Infrastructure는 런타임에 주입되므로 코드 레벨(Import)에서는 아무도 의존해서는 안 됨 (DIP의 핵심)
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()

                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain 계층은 Infrastructure 계층을 의존해서는 안 된다 (DIP 준수)")
    void domainShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .because("도메인(Domain) 계층은 인프라(Infrastructure) 구현체로부터 완전히 격리되어야 합니다. (DIP 위반)")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain 계층은 웹 관련 패키지(Spring Web 등)를 의존해서는 안 된다")
    void domainShouldNotDependOnWebLayer() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework.web..")
                .orShould().dependOnClassesThat().resideInAPackage("javax.servlet..")
                .orShould().dependOnClassesThat().resideInAPackage("jakarta.servlet..")
                .because("도메인 모델은 웹 요청/응답 처리 기술(Servlet, Web)에 의존할 수 없으며, 순수 자바 객체여야 합니다.")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Controller는 Service나 UseCase만 호출해야 하며, Repository를 직접 호출해서는 안 된다")
    void controllerShouldNotAccessRepositoryDirectly() {
        noClasses()
                .that().resideInAPackage("..presentation..")
                .or().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .orShould().dependOnClassesThat().resideInAPackage("..mapper..")
                .because("프레젠테이션 계층은 데이터 접근 계층을 직접 호출할 수 없으며, 반드시 비즈니스 로직(Service)을 경유해야 합니다.")
                .check(importedClasses);
    }
    
    @Test
    @DisplayName("Service는 Controller를 의존해서는 안 된다")
    void serviceShouldNotDependOnController() {
        noClasses()
                .that().resideInAPackage("..application..")
                .or().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..presentation..")
                .orShould().dependOnClassesThat().resideInAPackage("..controller..")
                .because("애플리케이션(Service) 계층은 상위 계층인 프레젠테이션(Controller) 계층에 의존할 수 없습니다. (순환 참조 방지)")
                .check(importedClasses);
    }

}
