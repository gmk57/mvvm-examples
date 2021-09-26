/**
 *
 * ./gradlew check
 *
 * ./gradlew testDebugUnitTest
 * ./gradlew connectedAndroidTest -PtestBuildType=debug
 * ./gradlew connectedAndroidTest -PtestBuildType=release
 * ./gradlew example-kt-01reactiveui:testDebugUnitTest --info
 * ./gradlew example-kt-04retrofit:dependencies --configuration releaseRuntimeClasspath
 *
 * sourcefile downloads sanity check:
 * co.early.fore.kt.net.apollo.CallProcessorApollo() //fore-kt-android-network
 * co.early.fore.net.NetworkingLogSanitizer() //fore-jv-network
 * co.early.fore.kt.net.NetworkingLogSanitizer() //fore-kt-network
 * co.early.fore.kt.adapters.NotifyableImp() //fore-kt-android-adapters
 * co.early.fore.adapters.CrossFadeRemover() //fore-jv-android-adapters
 * co.early.fore.kt.core.delegate.DebugDelegateDefault() //fore-kt-android-core
 * co.early.fore.kt.core.logging.SystemLogger() //fore-kt-core
 * co.early.fore.core.testhelpers.CountDownLatchWrapper() //fore-jv-core
 *
 * ./gradlew clean
 * ./gradlew publishToMavenLocal
 * ./gradlew publishReleasePublicationToMavenCentralRepository --no-daemon --no-parallel
 *
 * ./gradlew :buildEnvironment
 * ./gradlew :fore-kt-core:dependencies
 *
 * bundle exec jekyll serve
 *
 * bundle lock --update
 * bundle install
 */
import co.early.fore.Shared
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing
import java.net.URI

apply(plugin = "maven-publish")
apply(plugin = "signing")

val LIB_ARTIFACT_ID: String? by project
val LIB_DESCRIPTION: String? by project

println("[$LIB_ARTIFACT_ID android lib publish file]")

group = "${Shared.Publish.LIB_GROUP}"
version = "${Shared.Publish.LIB_VERSION_NAME}"

project.afterEvaluate {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {

                groupId = "${Shared.Publish.LIB_GROUP}"
                artifactId = LIB_ARTIFACT_ID
                version = "${Shared.Publish.LIB_VERSION_NAME}"

                artifact(tasks["bundleReleaseAar"])
                artifact(project.tasks["androidSourcesJar"])

                pom {
                    name.set("${Shared.Publish.PROJ_NAME}")
                    description.set(LIB_DESCRIPTION)
                    url.set("${Shared.Publish.POM_URL}")

                    licenses {
                        license {
                            name.set("${Shared.Publish.LICENCE_NAME}")
                            url.set("${Shared.Publish.LICENCE_URL}")
                        }
                    }
                    developers {
                        developer {
                            id.set("${Shared.Publish.LIB_DEVELOPER_ID}")
                            name.set("${Shared.Publish.LIB_DEVELOPER_NAME}")
                            email.set("${Shared.Publish.LIB_DEVELOPER_EMAIL}")
                        }
                    }
                    scm {
                        connection.set("${Shared.Publish.POM_SCM_CONNECTION}")
                        developerConnection.set("${Shared.Publish.POM_SCM_CONNECTION}")
                        url.set("${Shared.Publish.POM_SCM_URL}")
                    }

                    withXml {
                        val dependenciesNode = asNode().appendNode("dependencies")

                        // List all compile dependencies and write to POM
                        fun addDependency(dep: Dependency, scope: String) {
                            if (dep.group == null || dep.version == null || dep.name == "unspecified") {
                                return
                            } // Ignore invalid dependencies.

                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", dep.group)
                            dependencyNode.appendNode("artifactId", dep.name)
                            dependencyNode.appendNode("version", dep.version)
                            dependencyNode.appendNode("scope", scope)
                        }

                        // List all "api" dependencies (for new Gradle) as "compile" dependencies.
                        configurations.getByName("api").dependencies.forEach { addDependency(it, "compile") }
                        // List all "implementation" dependencies (for new Gradle) as "runtime" dependencies.
                        configurations.getByName("implementation").dependencies.forEach { addDependency(it, "runtime") }
                    }
                }
            }
        }
        repositories {
            maven {
                name = "mavenCentral"

                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
                val repoUrl = if (Shared.Publish.LIB_VERSION_NAME.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

                url = URI(repoUrl)

                credentials {
                    username = "${Shared.Secrets.MAVEN_USER}"
                    password = "${Shared.Secrets.MAVEN_PASSWORD}"
                }
            }
        }
    }

    configure<SigningExtension> {

        extra["signing.keyId"] = "${Shared.Secrets.SIGNING_KEY_ID}"
        extra["signing.password"] = "${Shared.Secrets.SIGNING_PASSWORD}"
        extra["signing.secretKeyRingFile"] = "${Shared.Secrets.SIGNING_KEY_RING_FILE}"

        val pubExt = checkNotNull(extensions.findByType(PublishingExtension::class.java))
        val publication = pubExt.publications["release"]
        sign(publication)
    }
}
