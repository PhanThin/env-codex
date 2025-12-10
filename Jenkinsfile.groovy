import groovy.transform.Field

@Field def useImage = true
@Field def useArtifact = false
@Field def isBackend = true
@Field def harborProject = "evn"
@Field def nexusGroupID = "EVN"
// @Field def changedServiceList = [] // Use when code base includes multiple services

// WARNING! DO NOT MODIFY NAME/PARAMS OF ORIGINAL FUNCTIONS
def getServiceList(){
    def listService = []
    return listService
}

def buildService() {
     stage('Build service') {
        try {
            sh """
                docker build -f docker/Dockerfile -t evn/backend:latest .
            """
        } catch (err) {
            error 'Build Failure'
        }
    }
}

def unitTestAndCodeCoverage(buildType){
    stage("Checkout source code"){
        jenkinsfile_utils.checkoutSourceCode(buildType)
    }
    stage('Run unit test') {
        try {
             echo "Run unit test"
        sh """
        export JAVA_HOME=/home/app/server/jdk-21.0.2
        java -version
        mvn -v 
        cd docker && mvn clean install -DskipTests
        ls -alh
        cd ..
        ls -alh
        mvn clean test org.jacoco:jacoco-maven-plugin:0.8.11:report
        """
        echo "code coverage done"
       
        catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
            jacoco([
                classPattern: 'target/classes',
                sourcePattern: 'src/main/java'
            ])
        }
        } catch (err) {
            String message = 'Error when test Unit Test'
            echo message
            env.UNIT_TEST_RESULT_STR += message
            throw err
        }
    }
}

def deployDevTest(version){
    // echo "Deploy to server develop"
    // echo "Version to deploy: $version"
    // checkoutSource("http://gitlab.os.gpdn.net/PVN/deployment/deployment.git","main")
    // def folder = sh(script: 'pwd', returnStdout: true)
    // env.buildFolderResult = folder.trim()
    // def deploy_template = load 'Jenkinsfile.groovy'
    // deploy_template.release2k8sTemplate("dev","pvn","code-base",version)
}

def deployProduct(service,version){
    echo "Deploy to server production"
    echo "Version to deploy: $version"
    checkoutSource("http://gitlab.os.gpdn.net/PVN/deployment/deployment.git","main")
    def folder = sh(script: 'pwd', returnStdout: true)
    env.buildFolderResult = folder.trim()
    def deploy_template = load 'Jenkinsfile.groovy'
    deploy_template.release2k8sTemplate("product","pvn","code-base",version)
}

def fortifyScan(){
    jenkinsfile_utils.fortifyScanStage(
        [
            serviceName : 'backend',
            sourcePathRegex : './**/*'
        ]
    )
}

def pushImage(){
    jenkinsfile_utils.pushImageToHarbor(
        [
            // credentialID : "<place your Harbor credential ID stored in Jenkins here>", // using cicd_bot to push by default
            repo_name : "evn",
            image_name : "backend"
        ]
    )
}

def selfCheckService(){
    return true
}

def rollback(){
    echo "Define rollback plan here"
    return true
}

def autotestProduct(){
    return true
}

// ------------------------------------ Self-defined functions ------------------------------------
def checkoutSource(String gitlabUrl, String branch){
    checkout changelog: true, poll: true, scm: [
        $class                           :  'GitSCM',
        branches                         : [[name: "$branch"]],
        doGenerateSubmoduleConfigurations: false,
        extensions                       : [[$class: 'UserIdentity',
                                            email : 'vtsjenkinsadmin@viettel.com.vn', name: 'cicdBot'],
                                            [$class: 'CleanBeforeCheckout']],
        submoduleCfg                     : [],
        userRemoteConfigs                : [[credentialsId: "jenkins-gitlab-os",
                                            url          : "$gitlabUrl"]]
    ]
}

return this
