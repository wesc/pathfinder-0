# Pathfinder

Pathfinder-0 is a proof of concept [technology
tree](https://medium.com/@foresight_institute/growing-technology-trees-for-longevity-molecular-machines-neurotech-computing-and-space-75582b2526c0)
mapping application for submission to the [MapsMap
challenge](https://mapsmap.org/) from [Foresight
Institute](https://foresight.org/).

## To Run

You'll need Docker and Docker Compose installed. Check out this repo
and run:

```
$ docker-compose up --build
```

This will setup a database, frontend, and backend service, and
populate it with a little bit of demo data. It will take a
while. You'll know it's done when you see lines like:

```
web_1       | [:app] Configuring build.
web_1       | [:app] Compiling ...
web_1       | SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
web_1       | SLF4J: Defaulting to no-operation (NOP) logger implementation
web_1       | SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
web_1       | [:app] Build completed. (11285 files, 270 compiled, 0 warnings, 27.94s)
```

Once that is done, you can open up your browser to [this demo
page](http://localhost:8280/).

To quit, hit ctrl-c. And then _make sure to run_:

```
$ docker-compose down -v
```

Otherwise, Pathfinder will continue running and take up space on your machine.

## Demo Video

Short demo video [here](https://youtu.be/M3-afFgMjSU).

## Pathfinder-0: The Approach

Pathfinder proposes a structured way to enter map node and dependency
data, and to provide a identifiable address at which one could place
funding campaigns. In addition to this, it provides a serialized
output format, called a subtree, that can be ported from one app to
another.

A subtree is a small chunk of an overall map, and contains a set of
technologies that are defined to have dependencies on each
other. Pathfinder provide a sample commenting system, and the intent
is to have a mode of operation similar to Google Docs. You publish a
subtree to a community, and members can comment on technologies which
can be edited and resolved.

Technologies also in turn reference subtrees, thus forming a kind of
hierarchical structure. The development of a technology may be an
entire roadmap on its own.

Pathfinder consists of a web frontend, and a backend service called
the Foundry. The Foundry is the beginning a subtree catalog. In a
scaled up production setting, the Foundry would serve as a location
where people can place their subtree definitions, much like GitHub is
a location to place Git repos, which then enable searchability and
wide ranging referencing between subtrees. The expected operation is
that a team working on subtree A may not have any knowledge of a team
working on subtree B which depends on A. This decoupling enables a
subtree map to scale beyond its intended use case. Ie, team B's work
should be, from the perspective of map definition, permissionless.

If I'd had more time and a larger team, I would have implemented the
key feature: the ability to fork and merge modified subtrees like with
Git. This mode of working enables constant discussion and improvement
of subtrees.

## Pathfinder-1: 0's Approach Is Wrong

Near the end of development I realized that Pathfinder's approach may
be incorrect. Fooled by the aesthetics of nodes and edges, Pathfinder
sets out a rigid method for entering and structuring that
data. Rather, Pathfinder should be an unstructured system like a wiki,
where map developers can write out detailed supporting arguments for
technologies with images, videos, and other attachments. Embedded
within that text could be links to other technologies along with
metadata markers such as "dependency", "blocker", or "suggested". From
this unstructured text, we can then extract out a dependency graph and
build tools that enable us to better understand the structure. We can
call this Pathfinder-1.

Pathfinder-0's model is Google Docs and Git. Pathfinder-1's model is
Wikipedia. It's unclear to me which model is the correct one. That
said, in developing ambitious world changing technology, it seems that
perhaps you might move from deep detailed document to a graph such as
with [Foresight's Longevity
Tree](https://medium.com/@foresight_institute/growing-technology-trees-for-longevity-molecular-machines-neurotech-computing-and-space-75582b2526c0). In
fact, Civ's technology trees [work very well in wiki
form](https://civilization.fandom.com/wiki/List_of_technologies_in_Civ6).

## MapsMap Criticism

See [additional in-depth constructive criticism](./constructive-criticism.md).
