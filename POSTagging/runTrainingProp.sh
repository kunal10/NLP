TEST_SET='atis'
RESULTS_DIR='results'
TRAINING_PROP='0.7'
TRACE_DIR='trace'
MODEL_TYPE='crf'
EXP_TYPE='Iterations'

FEATURE_TYPE='ortho'
DATA_FILE='atis3.txt'
if [ $FEATURE_TYPE == 'ortho' ]
then
   DATA_FILE='atis3ortho.txt'
fi

if [ $TEST_SET == 'wsj' ]
then
    DATA_FILE='wsj0to1.txt'
fi

CLASS='SimpleTagger'
if [ $MODEL_TYPE == 'hmm' ]
then
    CLASS='HMMSimpleTagger'
fi 

echo "TEST_SET: $TEST_SET"
echo "RESULTS_DIR: $RESULTS_DIR"
echo "TRAINING_PROP: $TRAINING_PROP"
echo "TRACE_DIR: $TRACE_DIR"
echo "DATA_FILE: ${RESULTS_DIR}/${TEST_SET}/${DATA_FILE}"
echo "FEATURE_TYPE: $FEATURE_TYPE"

echo "Starting Iteration: $param"

time java -cp "mallet-2.0.8RC3/class:mallet-2.0.8RC3/lib/mallet-deps.jar" cc.mallet.fst.${CLASS} \
--train true --model-file $RESULTS_DIR/${TEST_SET}/$MODEL_TYPE/$FEATURE_TYPE/model${EXP_TYPE}${TRAINING_PROP} \
--training-proportion ${TRAINING_PROP} "--test" lab $RESULTS_DIR/${TEST_SET}/$DATA_FILE 2>> $TRACE_DIR/${TEST_SET}/$MODEL_TYPE/$FEATURE_TYPE/model${EXP_TYPE}${TRAINING_PROP}trace.txt 

echo "Finished $EXP_TYPE Iteration: $param"  
