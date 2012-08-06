# Summary #

sbt-autodeps allows you to build an index of all packages in all
dependencies of a group of sbt projects, and then query for imports
that the compiler can't find. So when you refer to a package that
you've used before in a previous project, sbt-autodeps will be able to
suggest which dependency to add to your sbt build file, without you
having to hunt around for it.

# Use #

## Compile the plugin ##

The plugin is currently not published anywhere, so first:

    sbt publish-local

It has been developed against sbt 0.11.3, but may work with other
versions.

## Install the plugin globally ##

Add to your `~/.sbt/plugins/plugins.sbt`:

    resolvers += "Apache Snapshots" at "https://repository.apache.org/content/repositories/snapshots/"

    addSbtPlugin("org.greenrd" % "sbt-autodeps" % "0.1")
    
## Add the necessary bits to each project's build.sbt ##

**FIXME**: This sucks

    import org.greenrd.sbt.autodeps._

    seq(AutoDepsPlugin.newSettings : _*)

    
## Index the dependencies of project(s) of interest ##

    cd my-project
    sbt
    autodeps-index
    
## Query for a missing import ##

    cd newer-project
    sbt
    autodeps-suggest com.company.foobar._
    
# Bugs, TODOs, etc. #

See the Issues page for this project on GitHub.

# Index file fragility #

The index file format is subject to change in future versions. But
it's only a cache, so if it can't be read, sbt-autodeps will just
ignore it and start afresh with an empty file (logging an info
message to let you know it's done that). So the most that will
happen is you'll have to do some reindexing.
