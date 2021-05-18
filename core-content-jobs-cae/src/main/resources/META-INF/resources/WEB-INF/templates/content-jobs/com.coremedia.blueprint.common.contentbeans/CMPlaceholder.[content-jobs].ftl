<#-- @ftlvariable name="contentJobs" type="de.bas.content.engine.ContentJobJanitor" -->
<h3>Scheduled Jobs</h3>
<style>
    table, th, td {
        border: solid;
        border-collapse: collapse;
        padding: .3em .5em;
    }
</style>
<table>
	<thead>
	<tr>
		<td>ID</td>
		<td>Time</td>
	</tr>
	</thead>
	<tbody>
  <#list contentJobs.taskList![] as scheduledFutureHolder>
		<tr>
			<td>
				<a href="/blueprint/servlet/dynamic/content-jobs/terminate/${scheduledFutureHolder.abstractContentJob.contentJobBean.contentId}?origUrl=${springMacroRequestContext.requestUri}">
					Terminate ${scheduledFutureHolder.abstractContentJob.contentJobBean.contentId}
				</a>
			</td>
			<td>${scheduledFutureHolder.abstractContentJob.contentJobBean.localSettings.getDate("start-at").getTime()?datetime}</td>
		</tr>
  </#list>
	</tbody>
</table>
