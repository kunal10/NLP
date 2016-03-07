echo 'Submitting HMM  job for 0to3'
condor_submit condorProp0to3.condor

echo 'Submitting Crf jobs for 0to3'
condor_submit condorCrf0to3Prop.condor
condor_submit condorCrfOrtho0to3Prop.condor
