package me.maxandroid.router.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        println("Router Plugin applied ${project.name}")

    }
}