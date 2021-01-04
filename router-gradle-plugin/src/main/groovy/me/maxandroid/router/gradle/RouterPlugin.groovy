package me.maxandroid.router.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlugin implements Plugin<Project> {

    private static final String ROUTER_NAME = "router"

    @Override
    void apply(Project project) {

        project.getExtensions().create(ROUTER_NAME, RouterExtension)

        project.afterEvaluate {

            RouterExtension extension = project[ROUTER_NAME]
            println(extension.wikiDir)

        }

    }
}