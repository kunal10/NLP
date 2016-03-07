echo 'Submitting Crf Ortho job for 0to1'
condor_submit condorCrfOrthoProp.condor

echo 'Submitting Crf Ortho jobs for Iterations'
condor_submit condorCrfOrthoIter30.condor
condor_submit condorCrfOrthoIter60.condor
condor_submit condorCrfOrthoIter90.condor
condor_submit condorCrfOrthoIter120.condor
