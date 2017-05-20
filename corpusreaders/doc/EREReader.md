The ERE corpus is based on discussion forums.  In addition to the
inherent noise in the text, the annotations are also noisy. Many
mention spans are off-by-one (including additional whitespace, or just
misaligned for unknown reasons). See e.g. mention 'baragatoni', 
mention m-569959cf_39_480 in file ENG_DF_001237_20150411_F0000008E.xml,
reported as char span (4970, 4980) whereas we identify it as 
(4971,4981). There is therefore some additional wrangling in the reader to 
check nearby offsets for matching spans. 

The CorpusReader hierarchy has been updated to allow separate source and
annotation directories for standoff annotation, per current LDC
standard distributions.



