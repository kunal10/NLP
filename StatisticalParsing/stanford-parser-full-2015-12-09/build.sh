javac -cp "stanford-parser.jar:slf4j-api.jar:." UnsupervisedDomainAdaptationDemo.java

java -cp "stanford-parser.jar:slf4j-api.jar:." UnsupervisedDomainAdaptationDemo \
  ~/Workspace/NLP/StatisticalParsing/stanford-parser-full-2015-12-09/data/wsjTrain 1000 \
  ~/Workspace/NLP/StatisticalParsing/stanford-parser-full-2015-12-09/data/brown 1000 \
  ~/Workspace/NLP/StatisticalParsing/stanford-parser-full-2015-12-09/data/brown