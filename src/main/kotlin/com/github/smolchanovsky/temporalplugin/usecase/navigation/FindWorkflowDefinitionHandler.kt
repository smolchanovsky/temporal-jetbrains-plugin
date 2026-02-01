package com.github.smolchanovsky.temporalplugin.usecase.navigation

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

data class FindWorkflowDefinitionRequest(
    val project: Project,
    val workflowType: String
) : Request<List<WorkflowMatch>>

class FindWorkflowDefinitionHandler(
    private val finderProvider: (Project) -> WorkflowDefinitionFinder
) : RequestHandler<FindWorkflowDefinitionRequest, List<WorkflowMatch>> {

    override suspend fun handle(request: FindWorkflowDefinitionRequest): List<WorkflowMatch> {
        val project = request.project
        val workflowType = request.workflowType
        val scope = GlobalSearchScope.projectScope(project)
        val finder = finderProvider(project)

        return DumbService.getInstance(project).runReadActionInSmartMode<List<WorkflowMatch>> {
            finder.findWorkflowMatches(project, workflowType, scope)
        }
    }
}
