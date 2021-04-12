def call(cfgLocation){

def tools = new org.devops.tools()

String srcUrl = "${env.srcUrl}"
String branchName = "${env.branchName}"

pipeline{
    //指定运行此流水线的节点
    agent {
         kubernetes{
        label "jenkins-slave"
        yaml '''
        apiVersion: v1
        kind: Pod
        metadata:
          name: jenkins-slave
        spec:
          containers:
          - name: jnlp
            image: 172.20.48.53/devops/jnlp-slave:v1  # docker pull jenkins/jnlp-slave
            imagePullPolicy: Always
            env: 
            - name: LANG
              value: "en_US.UTF-8"
            resources:
              requests:
                cpu: "100m"
                memory: "512Mi"
            volumeMounts:
              - mountPath: /var/run/docker.sock
                name: docker-sock
                readOnly: false
              - mountPath: /root/.m2/
                name: m2-cache
          dnsConfig:
            nameservers:
              - 114.114.114.114
          imagePullSecrets:
          - name: harbor-admin  
          volumes:
          - hostPath:
              path: /var/run/docker.sock
              type: File
            name: docker-sock
          - name: m2-cache
            persistentVolumeClaim:
              claimName: m2-cache-pvc
        ''' 
      }
    }
    
    options {
        timestamps()   //添加日志时间
        timeout(time: 1, unit: 'HOURS')   // 设置超时时间
    }

    //流水线的阶段
    stages{
        stage("获取代码"){
            steps{
                container('jnlp'){
                script{
                    tools.PrintMes('获取代码','green1')
                    
                    }
                }
            }
            
        }
        
        //阶段1 获取代码
        stage("变量初始化"){
            steps{
                container('jnlp'){
                    script{
                        tools.PrintMes('变量初始化','green1')
                        sh 'printenv'
                    }
                }
                
            }
        }
        stage("开始编译"){
            steps{
                container('jnlp'){
                    script{
                         tools.PrintMes('开始编译 mvn clean package -Dmaven.test.skip=true','green')
                        sh "mvn clean package -Dmaven.test.skip=true"
                    }
                }
                
            }
        }
        stage("静态代码扫描"){
            steps{
                container('jnlp'){
                    script{
                        tools.PrintMes('静态代码扫描','green')
                    }
                    
                }
                
            }
        }
        stage("类库依赖扫描"){
            steps{
                container('jnlp'){
                    script{
                        tools.PrintMes('类库依赖扫描','green')
                    }
                }
                
            }
        }
        stage("打包docker镜像"){
            steps{
                container('jnlp'){
                    script{
                       tools.PrintMes('打包docker镜像','green')
                    }
                }
                
            }
        }
        stage("推送镜像仓库"){
            steps{
                container('jnlp'){
                    script{
                        tools.PrintMes('推送镜像仓库','green')
                    }
                }
                
            }
        }
    }
    post {
        always{
            script{
                tools.PrintMes('流水线结束后，经常做的事情','green')
            }
        }
        
        success{
            script{
                tools.PrintMes('流水线成功后，要做的事情','green')
                currentBuild.description = '\n构建cheng成功'
            }
        
        }
        failure{
            script{
                tools.PrintMes('流水线失败后，要做的事情','green')
                currentBuild.description = '\n构建失败' 
            }
        }
        
        aborted{
            script{
                tools.PrintMes('流水线取消后，要做的事情','green')
            }
        
        }
    }
 }
}
