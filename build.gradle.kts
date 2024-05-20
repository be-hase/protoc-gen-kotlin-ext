val isOnCI = System.getenv()["GITHUB_ACTIONS"] != null

allprojects {
    group = "dev.hsbrysk"
    version = "0.0.2" + if (isOnCI) "" else "-SNAPSHOT"
}
