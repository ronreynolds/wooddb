# Wood-DB Project Overview
the goal of this project is many-fold:
- gather wood data (macroscopic ID characteristics, endgrain-photos, etc) from at least 2 sources:
  * wood-database.com
  * hobbithouseinc.com (meh; good source of endgrain photos probably; no good for wood characteristics)
- convert this data into a consistent format
  - stored as YAML
- import this data into a single database
  - MariaDB
    - schema TBD
- support searching of this database by various means 
  * starting with some kind of web page
  * probably adding some kind of REST-ful service
- support standard wood-ID keys (DAGs and a Q&A interface to identify a wood via a series of targeted questions)
  - need to find more keys; so far the only 2 i've found are:
    - https://research.fs.usda.gov/treesearch/60337 (ID of Central American Woods; pgs )
    - https://research.fs.usda.gov/treesearch/64547 (ID of Central American, Mexican, and Caribbean Woods; pgs 68 - 80)
      - turns out this is a super-set of the previous one so only going to use this one for now
- identify all woods i've used thus far
  - check a few against https://research.fs.usda.gov/fpl/identification just to be sure i'm doing things right

## Other Resources
- XyloPhone design - https://github.com/xylodojo/XyloPhone
  - Paper in which it was discussed - https://research.fs.usda.gov/download/treesearch/62840.pdf
- XyloTron app source (i think) - https://github.com/fpl-xylotron/frontiers-in-plant-science-2020a/tree/master
- XyloTron - https://www.frontiersin.org/journals/plant-science/articles/10.3389/fpls.2020.01015/full
- https://www.aphis.usda.gov/sites/default/files/cites.pdf
    - AMAZING collection of scientific to common name mappings! (chapter 7)