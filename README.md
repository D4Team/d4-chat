# D4 chat

D4 chat is a backend server application for chatting.

## Run application

1. Run postgres database. The simplest option is to go to the `docker` directory and run docker
   compose: `docker-compose up -d`.
2. Provide environment variables for app. As a convenient option, go to the `src/main/resources` directory and create
   the `params.conf` file with the following example data:

```
APP_HOST = localhost
APP_PORT = 8090

DB_HOST="localhost"
DB_PORT=5444
DB_NAME="d4chat-postgres"
DB_USER="postgres"
DB_PASSWORD="password"
```

3. Run the application by the following command `sbt run` or just from your IDE.

## Code linting and auto-refactoring

Keeping a code in a good shape is a huge help in development that leeds to the clear, readable and easily supporting
code. To simplify this, we can rely on different tools and partially automate the process.

### Scalac options

Scalac compiler options allow enabling different code checks to warn about undesired code constructions or usages.
To look at the currently used options go to the `build.sbt` file at the root folder and find a `scalacOptions` key in
the `commonSettings` variable.

For convenience, each of the options is supported with a comment to explain what this flag affects.

For the current project if any waring appear then a whole build will be failed. But sometimes we can wish to temporarily
disable it (for example, due to refactoring). In order to do it, add the `scalacOptions -= "-Xfatal-warnings"`
instruction to the sequence in `commonSettings` variable after the first `scalacOptions` key.

Also, sometimes we can't cope with warnings and would like to silence them. In this case, a problematic expression
should be annotated with `@nowarn`.

### Scalafmt

Scalafmt is a code formatter allows to keep a code in a consistent style following predefined rules.

These rules can be found in the `.scalafmt.conf` file at the root folder of the project.

More information about scalafmt and its rules can be found here: https://scalameta.org/scalafmt/

### Scalafix

Scalafix is a refactoring and linting tool allows to warn about linting problems and remove undesired code
constructions following predefined rules.

These rules can be found in the `.scalafix.conf` file at the root folder of the project.

More information about scalafix and its rules can be found here: https://scalacenter.github.io/scalafix/
