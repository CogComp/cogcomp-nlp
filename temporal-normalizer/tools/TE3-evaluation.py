#!/usr/bin/python 

## this program evaluates the performance of extracted events and temporal expressions, and the overall temporal relation performance 

## $ cd tools 

## > python TE3-evaluation.py gold_folder_or_file system_folder_or_filefile
## $ python TE3-evaluation.py data/gold data/system 
## # runs with debug level 0 and only reports the performance; also creates a temporary folder to create normalized files  

## > python TE3-evaluation.py gold_folder_or_file system_folder_or_filefile debug_level 
## $ python TE3-evaluation.py data/gold data/system 1  
## # based on the debug_level print debug information 

## > python TE3-evaluation.py gold_folder_or_file system_folder_or_filefile debug_level tmp_folder
## $ python TE3-evaluation.py data/gold data/system 1 tmp_folder
## # additionally creates the temporary folder to put normalized files, which could be used for later uses 


## usage: 
## to check the performance of a single file: 
##          python TE3-evaluation.py gold_file_path system_file_path debug_level
## to check the performace of all files in a gold folder: 
##          python TE3-evaluation.py gold_folder_path system_folder_path debug_level


## V 1.0 Naushad UzZaman, March 24, 2012  

import os 
import re
import sys 
import math
import commands 
import tempfile


gold_dir = '' 
system_dir = '' 

def get_arg(index):
    return sys.argv[index]

if len(sys.argv) > 3: 
    debug = float(sys.argv[3])
else: 
    debug = 0 



def extract_name(filename):
    parts = re.split('/', filename)
    length = len(parts)
    return parts[length-1]


def get_directory_path(path): 
    name = extract_name(path)
    dir = re.sub(name, '', path) 
    if dir == '': 
        dir = './'
    return dir 


directory_path = get_directory_path(sys.argv[0])


def create_tmp_folder(): 
## create temporary folder 
    if os.path.exists(directory_path+'tmp-to-be-deleted'): 
        command = 'rm -rf '+directory_path+'tmp-to-be-deleted/*'
        os.system(command) 

def copy_folders(): 
	global gold_dir 
	global system_dir

	gold_folder = sys.argv[1]
	system_folder = sys.argv[2]

	if len(sys.argv) <= 4: 
		tmp_folder = tempfile.mkdtemp() 
	elif len(sys.argv) > 4:
		tmp_folder = sys.argv[4] 
		if tmp_folder[-1] == '/': 
			tmp_folder = tmp_folder[:-1]
		command = 'mkdir '+tmp_folder 
		try:
			os.system(command) 
#    print tmp_folder 
			command = 'mkdir '+tmp_folder+'/gold'
			os.system(command) 
			command = 'mkdir '+tmp_folder+'/system'
			os.system(command) 

		except: 
			print 'Can not create folder '+ tmp_folder 
			exit(0) 


	if os.path.isdir(gold_folder) and os.path.isdir(system_folder): 
		if gold_folder[-1] != '/': 
			gold_folder += '/' 
		if system_folder[-1] != '/': 
			system_folder += '/' 

		try: 
			command = 'cp -r '+gold_folder+ ' '+tmp_folder+'/gold/'
			os.system(command) 
			command = 'cp -r '+system_folder+ ' '+tmp_folder+'/system/'
			os.system(command) 
			gold_dir = tmp_folder+'/gold/'
			system_dir = tmp_folder+'/system/'
		except: 
			print 'Can not copy to new folder '
			exit(0) 

	elif (not os.path.isdir(gold_folder)) and (not os.path.isdir(system_folder)): 
		command = 'cp '+gold_folder+ ' '+tmp_folder+'/gold/'
		os.system(command) 
		command = 'cp '+system_folder+ ' '+tmp_folder+'/system/'
		os.system(command)
		gold_dir = tmp_folder+'/gold/'
		system_dir = tmp_folder+'/system/'

	return tmp_folder 


def normalize_folders(): 
	global gold_dir 
	global system_dir

	if debug >= 1: 
		command = 'java -jar TimeML-Normalizer/TimeML-Normalizer.jar -d -a "'+gold_dir+';'+system_dir+'"'
	else: 
		command = 'java -jar TimeML-Normalizer/TimeML-Normalizer.jar -a "'+gold_dir+';'+system_dir+'"'
	os.system(command) 
    
    
    
def evaluate(tmp_folder): 

	if len(os.listdir(gold_dir)) != len(os.listdir(sys.argv[1])): 
		print 'Invalid TimeML XML file exists, NOT EVALUATING FILES\n\n'

	command = 'python evaluation-entities/evaluate_entities.py '+tmp_folder+'/gold-normalized/'+' '+tmp_folder+'/system-normalized/ '+str(debug)
	os.system(command) 

	if len(sys.argv) > 5: 
		evaluation_method = ' ' + sys.argv[5] # ' implicit_in_recall', 'acl11
	else: 
		evaluation_method = ''
	command = 'python evaluation-relations/temporal_evaluation.py '+tmp_folder+'/gold-normalized/'+' '+tmp_folder+'/system-normalized/ '+str(debug) +  evaluation_method 
	os.system(command) 


create_tmp_folder()
if debug >= 3: 
    print 'folder created'
tmp_folder = copy_folders() 
if debug >= 3: 
    print 'copy folder'
normalize_folders()
if debug >= 3: 
    print 'normalized'
evaluate(tmp_folder) 
if debug >= 3: 
    print 'evaluated'

if len(sys.argv) <= 4: 
    command = 'rm -rf '+tmp_folder    
    if debug >= 1: 
        print 'Deleting temporary folder', tmp_folder 
        print 'To keep the temporary folder, RUN: "python TE3-evaluation.py gold_folder_or_file system_folder_or_filefile debug_level tmp_folder"' 
    os.system(command) 
