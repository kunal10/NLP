# echo 'Submitting Hmm job for 0to1'
# condor_submit condorProp.condor

echo 'Submitting Hmm jobs for Iterations'
condor_submit condorIter30.condor
condor_submit condorIter60.condor
condor_submit condorIter90.condor
condor_submit condorIter120.condor

echo 'Submitting Crf job for 0to1'
condor_submit condorCrfProp.condor

echo 'Submitting Crf jobs for Iterations'
condor_submit condorCrfIter30.condor
condor_submit condorCrfIter60.condor
condor_submit condorCrfIter90.condor
condor_submit condorCrfIter120.condor
