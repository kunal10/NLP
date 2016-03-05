TEST='test'
TEST_SET='atis'
RESULTS_DIR=results/${TEST_SET}
TRAINING_PROP='0.8'
TRACE_DIR='trace'
DATA_FILE='atis3ortho.txt'
MODEL_TYPE='crf'

if [ $TEST_SET == 'wsj' ]
then
    DATA_FILE='wsj0to1.txt'
fi

echo "TEST_SET: $TEST_SET"
echo "RESULTS_DIR: $RESULTS_DIR"
echo "TRAINING_PROP: $TRAINING_PROP"
echo "TRACE_DIR: $TRACE_DIR"
echo "DATA_FILE: ${RESULTS_DIR}/${DATA_FILE}"

for seed in 0 1 2 3 4 5 6 7 8 9
do
   echo "Starting Iteration: $seed"
   java -cp "mallet-2.0.8RC3/class:mallet-2.0.8RC3/lib/mallet-deps.jar" cc.mallet.fst.SimpleTagger \
     --train true --model-file $RESULTS_DIR/$MODEL_TYPE/model${seed}ortho.txt
   --training-proportion ${TRAINING_PROP} "--test" lab $RESULTS_DIR/$DATA_FILE | tee $TRACE_DIR/$MODEL_TYPE/model${seed}orthotrace.txt 
   echo "Finished Iteration: $seed"  
done
