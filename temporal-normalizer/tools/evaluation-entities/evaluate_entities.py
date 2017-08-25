#!/usr/bin/python 

## this program evaluates the performance of extracted events and temporal expressions 
## input files: TimeML annotated documents 
## output: performance of events and temporal expression extraction 
## usage: 
## to check the performance of a single file: 
##          python evaluate_entities.py gold_file_path system_file_path 
## to check the performace of all files in a gold folder: 
##          python evaluate_entities.py gold_folder_path system_folder_path 

## warning: given the input file, the systems can only add XML tags of EVENT and TIMEX3. this evaluation program won't work properly if extra spaces, new lines are added.  

## debug = 0, prints the final performance for event and timex extraction  
## debug = 0.5, prints all numbers that explains how the system got the final numbers 
## debug = 1, prints all performances for all files 
## debug = 1.5, prints the incorrect events/timex and features  
## debug = 2, prints all relevant information to trace which entities systems are missing 

## *** final score *** 
## we give one score to capture the performance of event/timex extraction and their feature extraction 
## 50% score is for event/timex extraction and 50% for feature extraction
## 50% of extraction is divided in to strict matching and relaxed matching equally. for example, if the gold annotation has "Sunday morning" and the system identifies "Sunday", then they will get credit in relaxed matching but not in exact matching. 
## for timex, 50% of attribute is divided equally between type and value 
## for event, class feature gets 25% and other features gets rest 25% equally 
## for event and timex, fscore (harmonic mean beween recall and precision) is calculated
## for features, the performance is recall of features, i.e. number of correct features/total features in gold data (total events) 


import os 
import re
import sys 
import math
import commands 


def get_arg(index):
    return sys.argv[index]


#debug = 0.5#1.5
debug = float(get_arg(3))

directory_path = '' 

## evaluation weights 

# event weights 
# according to TimeML, events are one word. so strict and relaxed should give the same performance. this is making sure if some teams extracts more than one word events we still reward them. 
w_strict_event_fscore = 0.25 
w_relaxed_event_fscore = 0.25 
w_class = 0.5 # 0.25 
no_rest_features = 4
w_rest_features = 0.5 - w_class 
w_tense = w_rest_features/no_rest_features
w_pos = w_rest_features/no_rest_features
w_aspect = w_rest_features/no_rest_features
w_polarity = w_rest_features/no_rest_features

# timex weights 
w_strict_timex_fscore = 0.25 
w_relaxed_timex_fscore = 0.25 
w_type = 0.25 
w_value = 0.25 

## global variables for evaluation 
global_system_timex = 0 
global_gold_timex = 0 
global_system_event = 0 
global_gold_event = 0 
global_timex_strict_match4precision = 0 
global_timex_relaxed_match4precision = 0 
global_timex_strict_match4recall = 0 
global_timex_relaxed_match4recall = 0 
global_event_strict_match4precision = 0 
global_event_relaxed_match4precision = 0 
global_event_strict_match4recall = 0 
global_event_relaxed_match4recall = 0 
global_class_match = 0 
global_tense_match = 0 
global_pos_match = 0 
global_polarity_match = 0 
global_modality_match = 0 
global_aspect_match = 0 
global_type_match = 0 
global_value_match = 0 


class event(): 
    "defining event class" 
    def __init__(self): 
        self.eid = '' 
        self.eiid = '' 
        self.class2 = '' 
        self.tense = '' 
        self.aspect = '' 
        self.pos = '' 
        self.polarity = '' 
        self.modality = '' 
        self.text = '' 
        self.annotation = '' 

class timex_class(): 
    "defining timex class" 
    def __init__(self): 
        self.tid = '' 
        self.text = '' 
        self.type = '' 
        self.value = ''
        self.annotation = '' 


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

# get only the annotated texts inside of TEXT tags 
def get_text(filetext): 
    import xml.dom.minidom
    dom = xml.dom.minidom.parseString( filetext.encode( "utf-8" ) )
    xmlTag = dom.getElementsByTagName('TEXT')[0].toxml()
    xmlData=xmlTag.replace('<TEXT>\n','').replace('</TEXT>','')
    xmlData = re.sub('&quot;', '"', xmlData) 
    return xmlData, filetext  


def correct_tags(word): 
    print word
    if re.search('<EVENT[^>]*>', word): 
        if not re.match('<EVENT[^>]*>', word): 
            tmp = re.findall('<[a-zA-Z][^>]*>', word) 
            print tmp 
    return '' 
#    if re.search('</[^>]*>', word): 
#        if word

def correct_annotation(filetext): 
    "if there are some tags in the middle of the word, this function puts the tag outside of the word, e.g. re-<EVENT>emerged</EVENT> => <EVENT>re-emerged</EVENT>" 
    new_text = '' 
    prev = '' 
    after_bl = '' 
    for line in filetext.split('\n'):
        if re.search('<(EVENT|TIMEX)[^>]*>', line): 
            if prev != '<internal_BL>' and prev != '<internal_NL>' and re.search('[\-]+', prev):
                tmp_foo = '' 
                entities = re.findall('<[a-zA-Z][^>]*>', line) 
                for each in entities: 
                    tmp_foo += each 
                tmp_foo += re.sub('\n', '', after_bl) 
                tmp_foo += re.sub('<[a-zA-Z][^>]*>', '', line) 
                after_bl = '' 
                line = tmp_foo 

        after_bl += line + '\n' 
        if line == '<internal_BL>' or line == '<internal_NL>': 
            new_text += after_bl 
            after_bl = '' 
        prev = line 
    new_text += after_bl 

#    after_tag_end = '' 
#    text_array = new_text.split('\n')
#    for i in range(0, len(text_array)): 
#        line = text_array[i]
#        if re.search('</[ET][^>]*>', line): 
#            if i < len(text_array): 
#                next = text_array[i+1]
#                if next != '<internal_BL>' and next != '<internal_NL>' and re.search('[a-zA-Z\-]+', next):
#                    print next 
#                    i += 1 
        
    return new_text 



def read_file(filename): 
    import codecs
    fileObj = codecs.open( filename, "r", "utf-8" )
    filetext = fileObj.read()
    fileObj.close() 
    ## changing .</TIMEX3> to </TIMEX3>. for all cases 
    filetext = re.sub('&quot;', '"', filetext) 

    return filetext


def get_tokenized_annotated_text(file): 
    "returns the tokenized annotated file"
    annotated_text, filetext = get_text(read_file(file))
    if debug >= 4: 
        print 'events on annotated text'
        print re.findall('<EVENT[^>]*>', annotated_text)
    global directory_path 
#    command = 'echo "' + annotated_text +'" | ' + directory_path + "tokenize.pl -e -w -a " + directory_path + "english-abbreviations " 
    out = open('foo.txt', 'w')
    out.write(annotated_text) 
    out.close()
#    command = 'echo "' + annotated_text +'" | ' + directory_path + "tokenize.pl -e -w -a " + directory_path + "english-abbreviations " 
    command = directory_path + "tokenize.pl -e -w -a " + directory_path + "english-abbreviations " + 'foo.txt' 

    tokenized_annotated_orig = commands.getoutput(command)
    tokenized_annotated = tokenized_annotated_orig 
#    print tokenized_annotated_orig
    tokenized_annotated = re.sub(r'(<EVENT[^>]*>)\n', r'\1', tokenized_annotated)
    tokenized_annotated = re.sub(r'\n(</EVENT>)', r'\1', tokenized_annotated)
    tokenized_annotated = re.sub(r'(<SIGNAL[^>]*>)\n', r'\1', tokenized_annotated)
    tokenized_annotated = re.sub(r'\n(</SIGNAL>)', r'\1', tokenized_annotated)

    tokenized_annotated = re.sub(r'(<TIMEX3[^>]*>)\n', r'\1', tokenized_annotated)
    tokenized_annotated = re.sub(r'\n(</TIMEX3>)', r'\1', tokenized_annotated)
#    tokenized_annotated = re.sub("(<[^>]*>)\'\'([a-zA-Z0-9]+)", r"''\1\2", tokenized_annotated)
    tokenized_annotated = correct_annotation(tokenized_annotated)
    print tokenized_annotated
    return tokenized_annotated, filetext, annotated_text 

def get_entity_value(line, entity):
    "given the event annotation, get the value for the features, e.g. <EVENT class=\"OCCURRENCE\" eid=\"e3\"> would return e3 for eid"
    if re.search(entity+'="[^"]*"', line): 
        entity_text = re.findall(entity+'="[^"]*"', line)[0]
        entity_text = re.sub(entity+'=', '', entity_text).strip('"') 
    else: 
        entity_text = entity 
    return entity_text 

def remove_tags(text):
    text = re.sub('<internal_BL>', ' ', text) 
    text = re.sub('<internal_NL>', '\n', text) 
    return re.sub('<[^>]*>', '', text) 

def index_entities(tokenized, filetext): 
    "index all events and timexes to compare for evaluation" 
    tokenized = tokenized.split('\n') 
    event_list = {} 
    token2event = {} 
    timex_list = {} 
    token2timex = {} 
    event_attributes = {} 
    timex_attributes = {} 

    makeinstances = re.findall('<MAKEINSTANCE[^>]*>', filetext)

    # get event features 
    for instance in makeinstances: 
        if re.search('eventID', instance): 
            x = event() 
            x.eid = get_entity_value(instance, 'eventID')
            if re.search('eiid=', instance): 
                x.eiid = get_entity_value(instance, 'eiid')
            if re.search('class=', instance): 
                x.class2 = get_entity_value(instance, 'class')
            if re.search('tense=', instance): 
                x.tense = get_entity_value(instance, 'tense')
            if re.search('aspect=', instance): 
                x.aspect = get_entity_value(instance, 'aspect')
            if re.search('polarity=', instance): 
                x.polarity = get_entity_value(instance, 'polarity')
            if re.search('pos=', instance): 
                x.pos = get_entity_value(instance, 'pos')
            if re.search('modality=', instance): 
                x.modality = get_entity_value(instance, 'modality')
            event_attributes[x.eid] = x 
        else: 
            continue 
            
    continue_event = 'false' 
    continue_timex = 'false' 
    timex_list_ordered = '' 
    event_list_ordered = '' 
    for i in range(0, len(tokenized)): 
        ## index events 
        if re.search('<EVENT', tokenized[i]): 
            eid = get_entity_value(tokenized[i], 'eid')
            event_list[eid] = [] 
            event_list[eid].append(i)  
            continue_event = 'true'
            token2event[i] = 'B-'+eid
            event_list_ordered += eid + '\n' 
            if eid in event_attributes:
                x = event_attributes[eid]
            else:
                x = event() 
                x.eid = eid 
            instance = tokenized[i]
            if re.search('eiid=', instance): 
                x.eiid = get_entity_value(instance, 'eiid')
            if re.search('class=', instance): 
                x.class2 = get_entity_value(instance, 'class')
            if re.search('tense=', instance): 
                x.tense = get_entity_value(instance, 'tense')
            if re.search('aspect=', instance): 
                x.aspect = get_entity_value(instance, 'aspect')
            if re.search('polarity=', instance): 
                x.polarity = get_entity_value(instance, 'polarity')
            if re.search('pos=', instance): 
                x.pos = get_entity_value(instance, 'pos')
            if re.search('modality=', instance): 
                x.modality = get_entity_value(instance, 'modality')
            x.text += instance + ' '
            event_attributes[eid] = x 
        
        if re.search('</EVENT', tokenized[i]):
            continue_event = 'false' 
            if not i in event_list[eid]: 
                event_list[eid].append(i)
                token2event[i] = 'I-'+eid
        
        if continue_event == 'true': 
            if not i in event_list[eid]: 
                event_list[eid].append(i)
                token2event[i] = 'I-'+eid

        ## index timex 
        if re.search('<TIMEX', tokenized[i]): 
            tid = get_entity_value(tokenized[i], 'tid')
            timex_list[tid] = [] 
            timex_list[tid].append(i)  
            continue_timex = 'true'
            token2timex[i] = 'B-'+tid
            timex_list_ordered += tid + '\n' 
            if tid in timex_attributes: 
                y = timex_attributes[tid] 
            else: 
                y = timex_class() 
            y.tid = tid 
            instance = tokenized[i]
            if re.search('type=', instance): 
                y.type = get_entity_value(instance, 'type')
            if re.search('value=', instance): 
                y.value = get_entity_value(instance, 'value')
#            y.text += tokenized[i]
            timex_attributes[tid] = y 
                
        if re.search('</TIMEX', tokenized[i]):
            y = timex_attributes[tid]
            y.text += tokenized[i]
            timex_attributes[tid] = y 
            continue_timex = 'false' 
            if not i in timex_list[tid]: 
                timex_list[tid].append(i)
                token2timex[i] = 'I-'+tid
        
        if continue_timex == 'true': 
            y = timex_attributes[tid]
            y.text += tokenized[i]
            timex_attributes[tid] = y 
            if not i in timex_list[tid]: 
                timex_list[tid].append(i)
                token2timex[i] = 'I-'+tid

    return event_list, token2event, event_attributes, timex_list, token2timex, timex_attributes, event_list_ordered, timex_list_ordered 


def remove_tokenizer_tags(word): 
    word = re.sub('<internal_BL>', ' ', word)
    word = re.sub('<internal_NL>', '\n', word)
    return word 


def compute_precision_recall(gold_event, gold_timex, system_event, system_timex): 
    "given the indexed events and timex, this module computes the precision and recall"
    global global_system_timex 
    global global_gold_timex
    global global_system_event
    global global_gold_event
    global global_timex_strict_match4precision
    global global_timex_relaxed_match4precision
    global global_timex_strict_match4recall
    global global_timex_relaxed_match4recall
    global global_event_strict_match4precision
    global global_event_relaxed_match4precision 
    global global_event_strict_match4recall
    global global_event_relaxed_match4recall

    global global_class_match 
    global global_tense_match
    global global_pos_match
    global global_polarity_match
    global global_modality_match
    global global_aspect_match
    global global_type_match 
    global global_value_match 



    ## event recall computation 
    if debug >= 1.5: 
        print '\n\nEVENT RECALL computation'
        print 'For each event in gold annotation, compare system annotation\n'
    total_event_strict_match4recall = 0 
    total_event_relaxed_match4recall = 0 
    total_class_match = 0 
    total_tense_match = 0 
    total_pos_match = 0 
    total_polarity_match = 0 
    total_modality_match = 0 
    total_aspect_match = 0 
    system_event_handled = {} 

    for eid in gold_event: 
        if gold_event[eid].text.strip() == '': 
            continue 

        if debug >= 2: 
            print ' gold annotation:', (gold_event[eid].annotation).encode('ascii','ignore')

        if eid in system_event and system_event[eid].text.strip() != '': 
            total_event_relaxed_match4recall += 1 
            match_word = '-- relaxed match' 
            g = gold_event[eid]
            s = system_event[eid]
            if gold_event[eid].text == system_event[eid].text: 
                total_event_strict_match4recall += 1 
                match_word = '-- strict match' 
                
            if debug >= 2: 
                print ' system annotation:', s.annotation, match_word
            if g.class2 == s.class2: 
                total_class_match += 1 
            if g.tense == s.tense: 
                total_tense_match += 1 
            if g.pos == s.pos: 
                total_pos_match += 1 
            if g.polarity == s.polarity: 
                total_polarity_match += 1 
            if g.modality == s.modality: 
                total_modality_match += 1 
            if g.aspect == s.aspect: 
                total_aspect_match += 1 

            if debug >= 1.5: 
                if g.class2 != s.class2: 
                    print '  -> gold class:', g.annotation 
                    print '  -> system wrong class:', s.annotation 

        else:                
            if debug >= 1.5: 
                print ' gold annotation not found in system:', gold_event[eid].annotation

    total_gold_event = len(gold_event) 
    if total_gold_event != 0: 
        strict_event_recall = 1.0*total_event_strict_match4recall/total_gold_event 
        relaxed_event_recall = 1.0*total_event_relaxed_match4recall/total_gold_event 
    else: 
        strict_event_recall = 0 
        relaxed_event_recall = 0 

    if debug >= 1: 
        print '\nEVENT EXTRACTION RECALL PERFORMANCE'
        print 'Strict Recall:', strict_event_recall
        print 'Relaxed Recall:', relaxed_event_recall 
    if debug >= 2: 
        print 'total:', total_gold_event 
        print 'strict count:', total_event_strict_match4recall
        print 'relaxed count:', total_event_relaxed_match4recall
        
    if debug >= 1: 
        print '\nEVENT FEATURE EXTRACTION PERFORMANCE'
        print 'total gold events or total features in gold data:', total_gold_event
        print 'total matching events:', total_event_strict_match4recall
        if total_event_strict_match4recall != 0: 
            print 'class accuracy:', total_class_match, ', performance', total_class_match*1.0/total_event_strict_match4recall
            print 'tense accuracy:', total_tense_match, ', performance', total_tense_match*1.0/total_event_strict_match4recall
            print 'aspect accuracy:', total_aspect_match, ', performance', total_aspect_match*1.0/total_event_strict_match4recall
            print 'pos accuracy:', total_pos_match, ', performance', total_pos_match*1.0/total_event_strict_match4recall
            print 'polarity accuracy:', total_polarity_match, ', performance', total_polarity_match*1.0/total_event_strict_match4recall
        else: 
            print 'all features: 0' 



   # timex precision computation 
    if debug >= 1.5: 
        print '\n\nEVENT PRECISION computation'
        print 'For each event in system annotation, compare gold annotation\n'
    total_event_strict_match4precision = 0 
    total_event_relaxed_match4precision = 0 
    gold_event_handled = {} 

    for eid in system_event: 
        if system_event[eid].text.strip() == '': 
            continue 
        if debug >= 2: 
            print ' system annotation:', system_event[eid].annotation            
        if eid in gold_event and gold_event[eid].text.strip() != '': 
            total_event_relaxed_match4precision += 1 
            match_word = '-- relaxed match' 
            g = gold_event[eid]
            s = system_event[eid]
            if g.text == s.text: 
                total_event_strict_match4precision += 1 
                match_word = '-- strict match' 
            
            if debug >= 2: 
                print ' gold annotation:', g.annotation, match_word

        else:
            if debug >= 1.5: 
                print ' system annotation not found in gold:', system_event[eid].annotation
 

    total_system_event = len(system_event) 
    if total_system_event != 0: 
        strict_event_precision = 1.0*total_event_strict_match4precision/total_system_event 
        relaxed_event_precision = 1.0*total_event_relaxed_match4precision/total_system_event 
    else: 
        strict_event_precision = 0 
        relaxed_event_precision = 0 


    if debug >= 1: 
        print '\nEVENT EXTRACTION PRECISION PERFORMANCE'
        print 'Strict Precision:', strict_event_precision
        print 'Relaxed Precision:', relaxed_event_precision
    if debug >= 2: 
        print 'total:', total_system_event 
        print 'strict count:', total_event_strict_match4precision
        print 'relaxed count:', total_event_relaxed_match4precision



    ## timex recall computation 
    if debug >= 1.5: 
        print '\n\nTIMEX RECALL computation' 
        print 'For each timex in gold annotation, compare system annotation\n'
    total_timex_strict_match4recall = 0 
    total_timex_relaxed_match4recall = 0 
    total_type_match = 0 
    total_value_match = 0 
    system_timex_handled = {} 

    for tid in gold_timex:
        if gold_timex[tid].text.strip() == '': 
            continue 

        if debug >= 2: 
            print ' gold annotation:', (gold_timex[tid].annotation).encode('ascii','ignore')

        if tid in system_timex and system_timex[tid].text.strip() != '':  
            total_timex_relaxed_match4recall += 1 
            match_word = '-- relaxed match' 
            g = gold_timex[tid]
            s = system_timex[tid]
            if gold_timex[tid].text == system_timex[tid].text: 
                total_timex_strict_match4recall += 1 
                match_word = '-- strict match' 
        
            if debug >= 2: 
                print ' system annotation:', s.annotation, match_word
            if g.type == s.type: 
                total_type_match += 1 
            if g.value == s.value: 
                total_value_match += 1 

            if debug >= 1.5: 
                if g.value != s.value: 
                    print '  -> gold value:', (g.annotation).encode('ascii','ignore')
                    print '  -> system wrong value:', (s.annotation).encode('ascii','ignore')

        else: 
            if debug >= 1.5: 
                print ' gold annotation not found in system:', (gold_timex[tid].annotation).encode('ascii','ignore')

    total_gold_timex = len(gold_timex) 
    if total_gold_timex != 0: 
        strict_timex_recall = 1.0*total_timex_strict_match4recall/total_gold_timex
        relaxed_timex_recall = 1.0*total_timex_relaxed_match4recall/total_gold_timex 
    else: 
        strict_timex_recall = 0 
        relaxed_timex_recall = 0

    if debug >= 1: 
        print '\nTIMEX EXTRACTION RECALL PERFORMANCE'
        print 'Strict Recall:', strict_timex_recall
        print 'Relaxed Recall:', relaxed_timex_recall             
    if debug >= 2: 
        print 'total:', total_gold_timex 
        print 'strict count:', total_timex_strict_match4recall
        print 'relaxed count:', total_timex_relaxed_match4recall

    if debug >= 1: 
        print '\nTIMEX FEATURE EXTRACTION PERFORMANCE'
        print 'total gold timex or total features in gold data:', total_gold_timex
        print 'total matching timex:', total_timex_relaxed_match4recall
        if total_timex_relaxed_match4recall != 0: 
            print 'type accuracy:', total_type_match, ', performance', total_type_match*1.0/total_timex_relaxed_match4recall
            print 'value accuracy:', total_value_match, ', performance', total_value_match*1.0/total_timex_relaxed_match4recall
        else: 
            print 'timex attribute performance:', 0 


    # timex precision computation 
    if debug >= 1.5: 
        print '\nTIMEX PRECISION computation'
        print 'For each timex in system annotation, compare gold annotation\n'
    total_timex_strict_match4precision = 0 
    total_timex_relaxed_match4precision = 0 
    gold_timex_handled = {} 

    for tid in system_timex: 
        if system_timex[tid].text.strip() == '': 
            continue 

        if debug >= 2: 
            print ' system annotation:', (system_timex[tid].annotation).encode('ascii','ignore')
        if tid in gold_timex and gold_timex[tid].text.strip() != '': 
            total_timex_relaxed_match4precision += 1 
            match_word = '-- relaxed match' 
            g = gold_timex[tid]
            s = system_timex[tid]
            if g.text == s.text: 
                total_timex_strict_match4precision += 1 
                match_word = '-- strict match' 
            
            if debug >= 2: 
                print ' gold annotation:', (g.annotation).encode('ascii','ignore'), match_word

        else:
            if debug >= 1.5: 
                print ' system annotation not found in gold:', (system_timex[tid].annotation).encode('ascii','ignore')
 

    total_system_timex = len(system_timex) 
    if total_system_timex != 0: 
        strict_timex_precision = 1.0*total_timex_strict_match4precision/total_system_timex 
        relaxed_timex_precision = 1.0*total_timex_relaxed_match4precision/total_system_timex 
    else: 
        strict_timex_precision = 0 
        relaxed_timex_precision = 0 

    if debug >= 1: 
        print '\nTIMEX EXTRACTION PRECISION PERFORMANCE'
        print 'Strict Precision:', strict_timex_precision
        print 'Relaxed Precision:', relaxed_timex_precision

    if debug >= 2: 
        print 'total:', total_system_timex 
        print 'strict count:', total_timex_strict_match4precision
        print 'relaxed count:', total_timex_relaxed_match4precision

    global_system_timex += total_system_timex 
    global_gold_timex += total_gold_timex 
    global_system_event += total_system_event 
    global_gold_event += total_gold_event 
    global_timex_strict_match4precision += total_timex_strict_match4precision
    global_timex_relaxed_match4precision += total_timex_relaxed_match4precision
    global_timex_strict_match4recall += total_timex_strict_match4recall
    global_timex_relaxed_match4recall += total_timex_relaxed_match4recall
    global_event_strict_match4precision += total_event_strict_match4precision
    global_event_relaxed_match4precision += total_event_relaxed_match4precision
    global_event_strict_match4recall += total_event_strict_match4recall
    global_event_relaxed_match4recall += total_event_relaxed_match4recall
    global_class_match += total_class_match
    global_tense_match += total_tense_match
    global_pos_match += total_pos_match
    global_polarity_match += total_polarity_match
    global_modality_match += total_modality_match
    global_aspect_match += total_aspect_match
    global_type_match += total_type_match 
    global_value_match += total_value_match


def strip_text(text): 
    text = re.sub('<[^>]*>', '', text) 
    return text 


def get_attributes(tml_file): 

    makeinstances = re.findall('<MAKEINSTANCE[^>]*>', tml_file)
    event_attributes = {}
    timex_attributes = {} 

    # get event features 
    for instance in makeinstances: 
        if re.search('eventID', instance): 
            x = event() 
            x.eid = get_entity_value(instance, 'eventID')
            if re.search('eiid=', instance): 
                x.eiid = get_entity_value(instance, 'eiid')
            if re.search('class=', instance): 
                x.class2 = get_entity_value(instance, 'class')
            if re.search('tense=', instance): 
                x.tense = get_entity_value(instance, 'tense')
            if re.search('aspect=', instance): 
                x.aspect = get_entity_value(instance, 'aspect')
            if re.search('polarity=', instance): 
                x.polarity = get_entity_value(instance, 'polarity')
            if re.search('pos=', instance): 
                x.pos = get_entity_value(instance, 'pos')
            if re.search('modality=', instance): 
                x.modality = get_entity_value(instance, 'modality')
            event_attributes[x.eid] = x 
        else: 
            continue 

    all_events = re.findall('<EVENT[^>]*>[^<]*</EVENT>', tml_file) 
    event_list = {} 
    for instance in all_events: 
        eid = get_entity_value(instance, 'eid')
        if eid in event_attributes: 
            x = event_attributes[eid]
        else: 
            x = event() 
            x.eid = eid 
        if re.search('eiid=', instance): 
            x.eiid = get_entity_value(instance, 'eiid')
        if re.search('class=', instance): 
            x.class2 = get_entity_value(instance, 'class')
        if re.search('tense=', instance): 
            x.tense = get_entity_value(instance, 'tense')
        if re.search('aspect=', instance): 
            x.aspect = get_entity_value(instance, 'aspect')
        if re.search('polarity=', instance): 
            x.polarity = get_entity_value(instance, 'polarity')
        if re.search('pos=', instance): 
            x.pos = get_entity_value(instance, 'pos')
        if re.search('modality=', instance): 
            x.modality = get_entity_value(instance, 'modality')
        x.text = re.sub('<[^>]*>', '', instance) 
        x.annotation = instance 
        event_attributes[eid] = x 

    all_timex = re.findall('<TIMEX3[^>]*>[^<]*</TIMEX3>', tml_file)   
    for instance in all_timex: 
        y = timex_class() 
        tid = get_entity_value(instance, 'tid')
        if tid == 't0': 
            continue 
        y.tid = tid 
        if re.search('type=', instance): 
            y.type = get_entity_value(instance, 'type')
        if re.search('value=', instance): 
            y.value = get_entity_value(instance, 'value')
        y.text = re.sub('<[^>]*>', '', instance) 
        y.annotation = instance 
        timex_attributes[tid] = y 

    return event_attributes, timex_attributes 

def evaluate_two_files(gold_file, system_file): 
	"evaluate two files" 
	if debug >= 1: 
		print '\n\nEVALUATE:', system_file, 'AGAINST GOLD ANNOTATION:', gold_file

	gold_onlytext, gold_tml = get_text(read_file(gold_file))
	system_onlytext, system_tml = get_text(read_file(system_file))
    
	if strip_text(gold_onlytext) != strip_text(system_onlytext):
		print 'TEXTS NOT SAME for', system_file, 'against', gold_file
		print 'EXITING THE EVALUATION SCRIPT' 
		gold_text_lines = strip_text(gold_onlytext).split('\n') 
		system_text_lines = strip_text(system_onlytext).split('\n') 
		for i in range(0, len(gold_text_lines)): 
			if i < len(gold_text_lines) and i < len(system_text_lines): 
				if gold_text_lines[i] != system_text_lines[i]: 
					print 'gold: "' + gold_text_lines[i] + '"'
					print 'syst: "' + system_text_lines[i] + '"'
					print '' 
			else: 
				print 'system missing gold\'s line number', i
				print 'gold: "' + gold_text_lines[i] + '"'
				print ''				
				
		if len(system_text_lines) > len(gold_text_lines): 
			for i in range(0, len(system_text_lines)): 
				if i >= len(gold_text_lines): 
					print 'gold doesn\'t have system\'s line number', i 
					print 'syst: "' + system_text_lines[i] + '"'
					print ''
				
				
		sys.exit(1)
		return '-1'


	gold_event, gold_timex = get_attributes(gold_tml)
	system_event, system_timex = get_attributes(system_tml)
	compute_precision_recall(gold_event, gold_timex, system_event, system_timex) 

    

def evaluate_two_files2(gold_file, system_file): 
	"eveluate two files" 
	if debug >= 1: 
		print '\n\nEVALUATE:', system_file, 'AGAINST GOLD ANNOTATION:', gold_file
	if debug >= 2: 
		print 'get the XML TEXT tag and annotate the gold annotation'
	gold_tokenized, gold_text, gold_just_text = get_tokenized_annotated_text(gold_file)
	print gold_just_text
	if debug >= 2: 
		print 'get the XML TEXT tag and annotate the system annotation'
	system_tokenized, system_text, system_just_text = get_tokenized_annotated_text(system_file)
    
	if strip_text(gold_just_text) != strip_text(system_just_text):
		print 'TEXTS NOT SAME for', system_file, 'against', gold_file
		print 'EXITING THE EVALUATION SCRIPT' 
		gold_text_lines = strip_text(gold_just_text).split('\n') 
		system_text_lines = strip_text(system_just_text).split('\n') 
		for i in range(0, len(gold_text_lines)): 
			if gold_text_lines[i] != system_text_lines[i]: 
				print gold_text_lines[i] 
				print system_text_lines[i]
				print '' 
		sys.exit(1)
		return '-1'


	# index events and timexes from the tokenized file  
	gold_event, gold_token2event, gold_event_attributes, gold_timex, gold_token2timex, gold_timex_attributes, gold_event_list, gold_timex_list = index_entities(gold_tokenized, gold_text)
	sys_event, sys_token2event, sys_event_attributes, sys_timex, sys_token2timex, sys_timex_attributes, sys_event_list, sys_timex_list = index_entities(system_tokenized, system_text)
    
	compute_precision_recall(gold_tokenized.split('\n'), gold_event, gold_token2event, gold_event_attributes, gold_timex, gold_token2timex, gold_timex_attributes, system_tokenized.split('\n'), sys_event, sys_token2event, sys_event_attributes, sys_timex, sys_token2timex, sys_timex_attributes, gold_event_list, gold_timex_list, sys_event_list, sys_timex_list) 

	# convert timex and even event to BIO format so that we can distinguish if its a new entity or same entity 

	return '' 


def evaluate_two_folders(gold, system):
    if gold[-1] != '/': 
        gold += '/' 
    if system[-1] != '/': 
        system += '/' 
    for file in os.listdir(gold):
        if os.path.isdir(gold+file):
            subdir = file+'/'
            if debug >= 1: 
                print 'Traverse files in Directory', gold+subdir
            evaluate_two_folders(gold+subdir, system+subdir)
        else:
            goldfile = gold +  file
            systemfile = system + file 
            if not re.search('DS_Store', file): 
                if debug >= 3: 
                    print goldfile, systemfile 
                evaluate_two_files(goldfile, systemfile)

def get_fscore(p, r): 
    if p+r == 0: 
        return 0 
    return 2.0*p*r/(p+r) 

def get_performance(): 
    if debug >= 0.5: 
        print '\nDETAIL PERFORMANCES' 

    ## event performance 
    if global_gold_event != 0: 
        global_strict_event_recall = 1.0*global_event_strict_match4recall/global_gold_event 
        global_relaxed_event_recall = 1.0*global_event_relaxed_match4recall/global_gold_event     
    else: 
        global_strict_event_recall = 0 
        global_relaxed_event_recall = 0 

    if debug >= 0.5: 
        print '\nEVENT EXTRACTION RECALL PERFORMANCE'
        print 'Strict Recall:', global_strict_event_recall
        print 'Relaxed Recall:', global_relaxed_event_recall 

    if global_system_event != 0: 
        global_strict_event_precision = 1.0*global_event_strict_match4precision/global_system_event 
        global_relaxed_event_precision = 1.0*global_event_relaxed_match4precision/global_system_event 
    else:
        global_strict_event_precision = 0 
        global_relaxed_event_precision = 0 
        
    if debug >= 0.5: 
        print '\nEVENT EXTRACTION PRECISION PERFORMANCE'
        print 'Strict Precision:', global_strict_event_precision
        print 'Relaxed Precision:', global_relaxed_event_precision

    
    if debug >= 0.5: 
        print '\nEVENT FEATURE EXTRACTION PERFORMANCE'
        print 'total gold events or total features in gold data:', global_gold_event
        print 'total gold events or total features in system data:', global_system_event
        print 'total matching events:', global_event_relaxed_match4recall
        if global_event_relaxed_match4recall != 0: 
            print 'matching class:', global_class_match, ', accuracy:', global_class_match*1.0/global_event_relaxed_match4recall, ', precision:', global_class_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_precision, ', recall:', global_class_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_recall
            print 'matching tense:', global_tense_match, ', accuracy:', global_tense_match*1.0/global_event_relaxed_match4recall, ', precision:', global_tense_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_precision, ', recall:', global_tense_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_recall
            print 'matching aspect:', global_aspect_match, ', accuracy:', global_aspect_match*1.0/global_event_relaxed_match4recall, ', precision:', global_aspect_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_precision, ', recall:', global_aspect_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_recall
            print 'matching pos:', global_pos_match, ', accuracy:', global_pos_match*1.0/global_event_relaxed_match4recall, ', precision:', global_pos_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_precision, ', recall:', global_pos_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_recall
            print 'matching polarity:', global_polarity_match, ', accuracy:', global_polarity_match*1.0/global_event_relaxed_match4recall, ', precision:', global_polarity_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_precision, ', recall:', global_polarity_match*1.0/global_event_relaxed_match4recall * global_relaxed_event_recall
        else: 
            print 'event attribute accuracy: 0' 
#        print 'correct modality:', global_modality_match, ', performance', global_modality_match*1.0/global_gold_event
        print ''		
        
    ## timex performance     
    if global_gold_timex != 0 : 
        global_strict_timex_recall = 1.0*global_timex_strict_match4recall/global_gold_timex
        global_relaxed_timex_recall = 1.0*global_timex_relaxed_match4recall/global_gold_timex 
    else: 
        global_strict_timex_recall = 0 
        global_relaxed_timex_recall = 0 

    if debug >= 0.5: 
        print '\nTIMEX EXTRACTION RECALL PERFORMANCE'
        print 'Strict Recall:', global_strict_timex_recall
        print 'Relaxed Recall:', global_relaxed_timex_recall             


    if global_system_timex != 0: 
        global_strict_timex_precision = 1.0*global_timex_strict_match4precision/global_system_timex 
        global_relaxed_timex_precision = 1.0*global_timex_relaxed_match4precision/global_system_timex 
    else: 
        global_strict_timex_precision = 0 
        global_relaxed_timex_precision = 0 


    if debug >= 0.5: 
        print '\nTIMEX EXTRACTION PRECISION PERFORMANCE'
        print 'Strict Precision:', global_strict_timex_precision
        print 'Relaxed Precision:', global_relaxed_timex_precision

    if debug >= 0.5: 
        print '\nTIMEX FEATURE EXTRACTION PERFORMANCE'
        print 'total gold timex or total features in gold data:', global_gold_timex
        print 'total gold timex or total features in system data:', global_system_timex
        print 'matching timex:', global_timex_relaxed_match4recall
        if global_timex_relaxed_match4recall != 0 : 
            print 'matching type:', global_type_match, ', accuracy:', global_type_match*1.0/global_timex_relaxed_match4recall, ', precision:', global_type_match*1.0/global_timex_relaxed_match4recall * global_relaxed_timex_precision, ', recall:', global_type_match*1.0/global_timex_relaxed_match4recall * global_relaxed_timex_recall
            print 'matching value:', global_value_match, ', accuracy:', global_value_match*1.0/global_timex_relaxed_match4recall, ', precision:', global_value_match*1.0/global_timex_relaxed_match4recall * global_relaxed_timex_precision, ', recall:', global_value_match*1.0/global_timex_relaxed_match4recall * global_relaxed_timex_recall
        else: 
            print 'timex attrubute accuracy: 0' 
        print '\n'		

    if global_system_event == 0 and global_gold_event == 0: 
        strict_event_fscore = 1 
        relaxed_event_fscore = 1 
        performance_class = 1 
        performance_tense = 1 
        performance_aspect = 1 
        performance_polarity = 1 

    else: 
        strict_event_fscore = get_fscore(global_strict_event_precision, global_strict_event_recall) 
        relaxed_event_fscore = get_fscore(global_relaxed_event_precision, global_relaxed_event_recall) 

        if global_event_relaxed_match4recall != 0: 
            performance_class = global_class_match*1.0/global_event_relaxed_match4recall*relaxed_event_fscore
            accuracy_class = global_class_match*1.0/global_event_relaxed_match4recall
            performance_tense = global_tense_match*1.0/global_event_relaxed_match4recall*relaxed_event_fscore
            performance_aspect = global_aspect_match*1.0/global_event_relaxed_match4recall*relaxed_event_fscore
            performance_pos = global_pos_match*1.0/global_event_relaxed_match4recall*relaxed_event_fscore
            performance_polarity = global_polarity_match*1.0/global_event_relaxed_match4recall*relaxed_event_fscore
        else: 
            performance_class = 0
            accuracy_class = 0 
            performance_tense = 0
            performance_aspect = 0
            performance_pos = 0
            performance_polarity = 0

    ## computing the event performance 
#    event_performance = strict_event_fscore*w_strict_event_fscore + relaxed_event_fscore*w_relaxed_event_fscore + performance_class*w_class + performance_tense*w_tense + performance_aspect*w_aspect + performance_pos*w_pos + performance_polarity*w_polarity
    event_extraction_performance = strict_event_fscore*0.5 + relaxed_event_fscore*0.5


    if global_system_timex == 0 and global_gold_timex == 0: 
        strict_timex_fscore = 1 
        relaxed_timex_fscore = 1
        performance_type = 1 
        performance_value = 1
    else: 
        strict_timex_fscore = get_fscore(global_strict_timex_precision, global_strict_timex_recall)
        relaxed_timex_fscore = get_fscore(global_relaxed_timex_precision, global_relaxed_timex_recall)

        if global_timex_relaxed_match4recall != 0: 
            performance_type = global_type_match*1.0/global_timex_relaxed_match4recall*relaxed_timex_fscore
            performance_value = global_value_match*1.0/global_timex_relaxed_match4recall*relaxed_timex_fscore
            accuracy_type = global_type_match*1.0/global_timex_relaxed_match4recall
            accuracy_value = global_value_match*1.0/global_timex_relaxed_match4recall

        else: 
            performance_type = 0
            performance_value = 0 
            accuracy_type = 0 
            accuracy_value = 0 
    ## computing the timex performance 
#    timex_performance = strict_timex_fscore*w_strict_timex_fscore + relaxed_timex_fscore*w_relaxed_timex_fscore + performance_type*w_type + performance_value*w_value
    timex_extraction_performance = strict_timex_fscore*0.5 + relaxed_timex_fscore*0.5 


    if debug >= 0:
		print '=== Event Performance ==='
		print 'Strict Match\tF1\tP\tR'
		print '\t\t' + str(round(strict_event_fscore*100, 2)) + '\t' + str(round(global_strict_event_precision*100, 2)) + '\t' + str(round(global_strict_event_recall*100, 2)) 
		
		print 'Relaxed Match\tF1\tP\tR'
		print '\t\t' + str(round(relaxed_event_fscore*100, 2)) + '\t' + str(round(global_relaxed_event_precision*100, 2)) + '\t' + str(round(global_relaxed_event_recall*100, 2)) 
		
		print 'Attribute F1\tClass\tTense\tAspect'
		print '\t\t' + str(round(performance_class*100, 2))+'\t'+str(round(performance_tense*100, 2))+'\t'+str(round(performance_aspect*100, 2))
		##print 'Overall Event Performance (Class F1 score):', str(round(performance_class*100, 2)) 
		print ''
		
		print '=== Timex Performance ==='
		print 'Strict Match\tF1\tP\tR'
		print '\t\t' + str(round(strict_timex_fscore*100, 2)) + '\t' + str(round(global_strict_timex_precision*100, 2)) + '\t' + str(round(global_strict_timex_recall*100, 2)) 
		
		print 'Relaxed Match\tF1\tP\tR'
		print '\t\t' + str(round(relaxed_timex_fscore*100, 2)) + '\t' + str(round(global_relaxed_timex_precision*100, 2)) + '\t' + str(round(global_relaxed_timex_recall*100, 2)) 
		
		print 'Attribute F1\tValue\tType'
		print '\t\t' + str(round(performance_value*100, 2))+'\t'+str(round(performance_type*100, 2))
		##print 'Overall Timex Performance (Value F1 score):', str(round(performance_value*100, 2))
		print ''

##		print 'EVENT\tOVERALL\t\tRecognition F1\tStrict\tRelaxed\tClass\tTense\tAspect'
##		print '\t'+str(round(performance_class*100, 2))+'\t\t'+str(round(event_extraction_performance*100, 2))+'\t\t'+str(round(strict_event_fscore*100, 2))+'\t'+str(round(relaxed_event_fscore*100, 2))+'\t'+str(round(performance_class*100, 2))+'\t'+str(round(performance_tense*100, 2))+'\t'+str(round(performance_aspect*100, 2))
 ##       print ''

##		print 'TIMEX\tOVERALL\t\tRecognition F1\tStrict\tRelaxed\tValue\tType'
##		print '\t'+str(round(performance_value*100, 2))+'\t\t'+str(round(timex_extraction_performance*100, 2))+'\t\t'+str(round(strict_timex_fscore*100, 2))+'\t'+str(round(relaxed_timex_fscore*100, 2))+'\t'+str(round(performance_value*100, 2))+'\t'+str(round(performance_type*100, 2))
##		print ''
    
    if debug == -1:
        print 'EVENT\tP\tR\tF1\tClass'
        print '\t'+str(round(global_relaxed_event_precision, 4))+'\t'+str(round(global_relaxed_event_recall, 4))+'\t'+str(round(relaxed_event_fscore, 4))+'\t'+str(round(accuracy_class, 4))
        print ''
        print 'TIMEX\tP\tR\tF1\tValue\tType'
        print '\t'+str(round(global_relaxed_timex_precision, 4))+'\t'+str(round(global_relaxed_timex_recall, 4))+'\t'+str(round(relaxed_timex_fscore, 4))+'\t'+str(round(accuracy_value, 4))+'\t'+str(round(accuracy_type, 4))

    if debug == -2:
        print 'EVENT\tP\tR\tF1\tClass'
        print '\t'+str(round(global_relaxed_event_precision, 4))+'\t'+str(round(global_relaxed_event_recall, 4))+'\t'+str(round(relaxed_event_fscore, 4))+'\t'+str(round(accuracy_class, 4))
        print ''
        print 'TIMEX\tP\tR\tF1\tSt F1\tValue\tType'
        print '\t'+str(round(global_relaxed_timex_precision, 4))+'\t'+str(round(global_relaxed_timex_recall, 4))+'\t'+str(round(relaxed_timex_fscore, 4))+'\t'+str(round(strict_timex_fscore, 4))+'\t'+str(round(accuracy_value, 4))+'\t'+str(round(accuracy_type, 4))

# take input from command line and give error messages 
# call appropriate functions to evaluate 
def input_and_evaluate(): 
    invalid = 'false' 
    if len(sys.argv) < 3: 
        invalid = 'true' 
    else: 
        arg1 = get_arg(1) 
        arg2 = get_arg(2) 
        global directory_path 
        directory_path = get_directory_path(sys.argv[0])

    # both arguments are directories 
    if invalid == 'false' and os.path.isdir(arg1) and os.path.isdir(arg2): 
        # for each files in gold folder, check the performance of that file in system folder 
        if debug >= 2: 
            print 'compare files in two folders' 
        evaluate_two_folders(arg1, arg2)
    elif invalid == 'false' and os.path.isfile(arg1) and os.path.isfile(arg2): 
        # compare the performance between two files 
        if debug >= 2: 
            print 'compare two files'
        evaluate_two_files(arg1, arg2)
    else: 
        invalid = 'true' 
        print 'INVALID INPUT FORMAT'
        print '\nto check the performance of a single file:\n\t python evaluate_entities.py gold_file_path system_file_path\n' 
        print 'to check the performace of all files in a gold folder:\n\t python evaluate_entities.py gold_folder_path system_folder_path \n\n'
    
    if invalid == 'false': 
        get_performance() 


input_and_evaluate()
