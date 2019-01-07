import java.time.LocalDateTime

eventCreateWarStart = { warName, stagingDir ->

    println "\n[Start add additional properties for war file]\n"

    def formatter = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
    def buildDateTimeStamp = formatter.format(new Date(System.currentTimeMillis()))

    def RevisionNumber = ant.antProject.properties."environment.GIT_COMMIT"?: ant.antProject.properties."environment.SVN_REVISION"

    if (!RevisionNumber) {
        try {
            def command = """git rev-parse HEAD"""
            def exec = command.execute()
            exec.waitFor()
            if (exec.exitValue() == 0) {
                RevisionNumber = exec.in.text
            }
        } catch (IOException e) {
        }
    }
    if (!RevisionNumber) {
        File entries = new File(basedir, '.svn/entries')
        if (entries.exists() && entries.text.split('\n').length>3) {
            evisionNumber = entries.text.split('\n')[3].trim()
        }
    }

    RevisionNumber = RevisionNumber?: 'UNKNOWN'

    def CheckedOutBranch = ant.antProject.properties."environment.GIT_BRANCH"?:ant.antProject.properties."environment.SVN_URL"

    if (!CheckedOutBranch) {
        try {
            def command = """git rev-parse --abbrev-ref HEAD"""
            def exec = command.execute()
            exec.waitFor()
            if (exec.exitValue() == 0) {
                CheckedOutBranch = exec.in.text
            }
        } catch (IOException e) {
        }
    }


    CheckedOutBranch = CheckedOutBranch?: 'UNKNOWN'

    ant.propertyfile(file: "${stagingDir}/WEB-INF/classes/application.properties") {
        entry(key:"build.DateTimeStamp", value: buildDateTimeStamp)
        entry(key:"repository.revision.number", value: RevisionNumber )
        entry(key:"repository.branch", value: CheckedOutBranch)
    }



    println "\n[End add additional properties for war file:\n DateTimeStamp:${buildDateTimeStamp}, RevisionNumber: ${RevisionNumber}, Checkedout Branch:${CheckedOutBranch}]\n"

}