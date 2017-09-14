# this program converts tempeval-2 format all tlink relations to timegraph 
# conventions: 
## parent = left side, child = right side 
## x < y, x's parent NIL, child y; y's parent x, child NIL

### changes 
## October 24: 
# change the main function from tempeval_to_timegraph_func to create_timegraph_from_weight_sorted_relations. earlier, we were finding the neighbours to travers. now we are reading the sorted list to traverse. 
# fixed a bug in the interval_rel_X_Y function, were missing some entities (check return 'semi-true' instances 
# added closure violation (tg.violated_relations) and remove from reduce graph list (tg.remove_from_reduce), removing same relations (tg.nonredundant), tg.final_relations = tg.nonredundant - tg.remove_from_reduce - tg.violated_relations 

import sys
import re
import Queue 
import time 
debug = 0

# variables for config file 
tlink_files = '' 
order = '' 
init_dct = '' 
consider_neighbor = ''
debug_verification = '' # 'true'
visualization = '' #'true' #'false' 
timegraph_debug = '' 
consider_direct_match = '' 

count_not_handled = 0
count_didnt_match_rel = 0

ignore_chain_history = 'false'
consider_DURING_as_SIMULTANEOUS = True 


def bar (index):
    #for arg in sys.argv:
    if debug >= 2: 
        print sys.argv[index]
    return sys.argv[index]

def extract_name(filename):
    parts = re.split('/', filename)
    length = len(parts)
    return parts[length-1]

## absolute path of basedir 
# basedir = '/Users/naushadzaman/Documents/work/research/URCS/temporal-ordering/experiments/TimeBank/Timebank-1-2/TimeBank_1_2/'

## relative path of basedir
#basedir = './'

#filename = bar(1)
#basedir = bar(2)
#corpus = bar(3)

all_tlinks = '' 
filename = ''
basedir = ''
config = '' 
name = '' 
dct = '' 



#filename = bar(1) 
#name = extract_name(filename)

# global variables for timegraph 
#base value 
base_value = 100000
#difference between two elements in chain 
diff = 1000 
# macro for NIL 
NIL = '-1'  

 
def get_feature(line): 
    words = line.split('=')
    if len(words) >= 1: 
        return words[1] 
    return '' 
    

## ignore this module currently 
def read_config(file, config_file): 
    global tlink_files
    global all_tlinks
    global system_tlinks
    global order
    global name 
    global filename 
    global basedir 
    global config 
    global dct
    global init_dct
    global consider_neighbor
    global debug_verification
    global visualization 
    global timegraph_debug
    global consider_direct_match


    filename = file
    config = config_file 
    text = open(config).read()
    for line in text.split('\n'): 
        if line.strip() != '':
            if not re.search('###', line):
                if re.search('tlink_files', line): 
                    tlink_files = get_feature(line)
                if re.search('order', line): 
                    order = get_feature(line) 
                if re.search('init_dct', line): 
                    init_dct = get_feature(line) 
                if re.search('consider_neighbor', line): 
                    consider_neighbor = get_feature(line) 
                if re.search('reference_annotation', line): 
                    reference_annotation = get_feature(line) 
                if re.search('system_output', line): 
                    system_output = get_feature(line) 
                if re.search('visualization', line): 
                    visualization = get_feature(line) 
                if re.search('debug_verification', line): 
                    debug_verification = get_feature(line) 
                if re.search('basedir', line): 
                    basedir = get_feature(line) 
                if re.search('timegraph_debug', line): 
                    timegraph_debug = get_feature(line)
                if re.search('consider_direct_match', line): 
                    consider_direct_match = get_feature(line)

##    name = re.sub('.tml', '', extract_name(filename))
    name = extract_name(filename)
##    name = re.sub('.txt', '', name)
    all_tlinks = basedir + 'data/'+reference_annotation+'/'+name#+'.txt'
    system_tlinks = basedir + 'data/'+system_output+'/'+name#+'.txt'
#    tlinks_event_event = basedir + 'data/'+ 'tinks-event-event/'+name#+'.txt'
#    tlinks_dct_event = basedir + 'data/'+ 'tlinks-dct-event/'+name#+'.txt'
#    tlinks_timex_event = basedir + 'data/'+ 'tlinks-timex-event/'+name#+'.txt'
#    tlinks_timex_timex = basedir + 'data/'+ 'tlinks-timex-timex/'+name#+'.txt'
    
#    dct = basedir + 'data/3-dct/'+name#+'.txt'
    


def getdct(): 
#    return 't0'
    text = open(dct).read() 
    text = text.strip()
    tid = text.split('\t')[2]
    return tid.strip()


class Node: 
    def __init__(self, id):
        self.id = id 
        self.chain = 0 
        self.pseudo = 0 
        self.child = '' 
        self.parent = '' 
        self.sibling = '' 
        self.p = '' 
        self.c = '' 


class Chain: 
    def __init__(self, dcc, cp):
        # directly connected chains => dcc
        self.dcc = str(dcc)
        # connection points 
        self.cp = cp 
        self.cp_to_chain = {} 
        self.cross_chain = {} 

class Timegraph: 
    def __init__(self):
        self.metagraph = {} 
        #this array's elements are each Node, which are start or end point of temporal entities, e.g. e1_s, t1_e, etc. 
        self.node_array = {} 

        # hash entity -> all TLINKS related to that entity 
        self.entity_to_lines = {} 
        # create a queue to keep all lines (TLINK) entries. 
        self.filetext_queue = Queue.Queue(0)
        # queue maintaining which entity to consider next 
        self.q = Queue.Queue(0)

        # variable to keep track of entities that has already been considered (in queue) 
        self.once_in_queue = "" 
        # lines already considered and added in the timegraph 
        self.entries_added = ""     
        #counter of chain 
        self.next_chain = 0     
        self.count_cross_chain = 0
        self.count_relation = 0 
        self.count_node = 0 
        ## relations that violates closure property, we want to remove it from our systems, but we keep it for systems which we want to evaluate 
        self.violated_relations = '' 
        ## extra relations that doesn't belong to reduced graph
        self.remove_from_reduce = '' 
        ## nonredundant is not considering same relations twice 
        self.nonredundant = ''
        ## for our system, we consider final_relations only 
        self.final_relations = '' 


def reverse_relation(rel): 
    if rel.upper() == 'BEFORE': 
        return 'AFTER'
    if rel.upper() == 'AFTER': 
        return 'BEFORE' 
    if rel.upper() == 'IBEFORE': 
        return 'IAFTER' 
    if rel.upper() == 'IAFTER': 
        return 'IBEFORE' 
    if rel.upper() == 'DURING': 
        return 'DURING_DURING_INV' 
    if rel.upper() == 'BEGINS': 
        return 'BEGUN_BY' 
    if rel.upper() == 'BEGUN_BY': 
        return 'BEGINS'
    if rel.upper() == 'ENDS': 
        return 'ENDED_BY' 
    if rel.upper() == 'ENDED_BY': 
        return 'ENDS' 
    if rel.upper() == 'INCLUDES': 
        return 'IS_INCLUDED' 
    if rel.upper() == 'IS_INCLUDED': 
        return 'INCLUDES' 
    if rel.upper() == 'IDENTITY' or rel.upper() == 'SIMULTANEOUS':
        return 'SIMULTANEOUS' 
    return rel.upper() 




def traverse_from_x_to_y(nx, x, ny, y, visited): 
    if nx.chain == ny.chain: 
        if nx.pseudo >= ny.pseudo:
            return 'true' 
        else: 
            # if the target (y) exists in the chain and before connection point then return unknown 
            return 'unknown' 
    else: 
        a = 1
        # this would be a recursive function 
        # find all connection after the start point (x) 
        # find direct connected chain 
        # keep track of visited nodes, add current x as visited.
        # recursively call next chain's connected point as start and with initial target (y) 
    



# X after Y 
def add_X_after_Y_metagraph(nx, x, ny, y, tg): 
#    global count_cross_chain
    tg.count_cross_chain += 1
#    global metagraph 
# traversing: 
# connection point, cp = metagraph[chain].cp 
# cp to another chain, cp_to_chain = metagraph[chain].cp_to_chain[cp] 
# cross chains (entities in other chain), 
    if not ny.chain in tg.metagraph: 
        # add dcc, cp in object creation 
        tg.metagraph[ny.chain] = Chain(nx.chain, y)
        tg.metagraph[ny.chain].cp_to_chain[y] = str(nx.chain) 
        tg.metagraph[ny.chain].cross_chain[y] = x 
    else: 
        # no use of dcc, same as cp_to_chain 
#        if not re.search(str(nx.chain), tg.metagraph[ny.chain].dcc):
#            tg.metagraph[ny.chain].dcc += ' ' + str(nx.chain)
        if not re.search(y, tg.metagraph[ny.chain].cp):
            tg.metagraph[ny.chain].cp += ' ' + y
        if y in tg.metagraph[ny.chain].cross_chain: 
            if not re.search(x, tg.metagraph[ny.chain].cross_chain[y]): 
                tg.metagraph[ny.chain].cross_chain[y] += ' ' + x 
        else: 
            tg.metagraph[ny.chain].cross_chain[y] = x 
        if y in tg.metagraph[ny.chain].cp_to_chain: 
            if not re.search(str(nx.chain), tg.metagraph[ny.chain].cp_to_chain[y]):
                tg.metagraph[ny.chain].cp_to_chain[y] += ' ' + str(nx.chain)
        else:
            tg.metagraph[ny.chain].cp_to_chain[y] = str(nx.chain)
    return tg 


def search_x_in_y(x, y): 
    return re.search(' '+x+' ', ' '+y+' ' ) 

def add_point_x_AFTER_y(x, y, tg): 
#    global next_chain 
    ny = tg.node_array[y]
    nx = Node(x) 
    if ny.child == NIL: 
        nx.chain = ny.chain 
        ny.child = x
        # for all siblings change the child as well 
        nx.parent = y 
        nx.child = NIL 
        nx.pseudo = ny.pseudo + diff 
    else: 
        nx.chain = tg.next_chain 
        tg.next_chain += 1 
        nx.pseudo = ny.pseudo + diff 
        nx.parent = NIL 
        nx.child = NIL 
        # add metagraph 
        tg = add_X_after_Y_metagraph(nx, x, ny, y, tg)
    return nx, tg 

def add_point_x_BEFORE_y(x, y, tg): 
#    global next_chain 
    # x < y 
    ny = tg.node_array[y]
    nx = Node(x) 
    if ny.parent == NIL: 
        nx.chain = ny.chain 
        ny.parent = x
        # for all siblings change the child as well 
        nx.parent = NIL 
        nx.child = y 
        nx.pseudo = ny.pseudo - diff 
    else: 
        nx.chain = tg.next_chain 
        tg.next_chain += 1 
        nx.pseudo = ny.pseudo - diff 
        nx.parent = NIL 
        nx.child = NIL 
        # add metagraph 
        tg = add_X_after_Y_metagraph(ny, y, nx, x, tg)
    return nx, tg

               
def add_point_x_EQUAL_y(x, y): 
    ny = tg.node_array[y]
    nx = Node(x) 
    nx = ny 

def add_point_x_DURING_y_z(x, y, z, tg): 
#    global next_chain 
    # y < x < z 
    ny = tg.node_array[y]
    nz = tg.node_array[z] 
    nx = Node(x)
    if ny.chain == nz.chain and search_x_in_y(z, ny.child) and search_x_in_y(y, nz.parent): 
        nx.chain = ny.chain 
        ny.child = x 
        nz.parent = x 
        nx.parent = y 
        nx.child = z 
        nx.pseudo = ny.pseudo + (nz.pseudo - ny.pseudo)/2 
    elif ny.child == NIL: 
        nx.chain = ny.chain 
        ny.child = x 
        nx.parent = y 
        nx.pseudo = ny.pseudo + diff 
        nx.child = NIL 
        tg = add_X_after_Y_metagraph(nz, z, nx, x, tg) 
    elif nz.parent == NIL: 
        nz.parent = x 
        nx.child = z
        nx.chain = nz.chain 
        nx.pseudo = nz.pseudo - diff 
        tg = add_X_after_Y_metagraph(nx, x, ny, y, tg) 
    else: 
        nx.chain = tg.next_chain 
        tg.next_chain += 1 
        # todo: add parent child 
#        nz.parent += ' '+x 
#        nx.child += ' '+z 
        nx.pseudo = ny.pseudo + (nz.pseudo - ny.pseudo)/2 
        tg = add_X_after_Y_metagraph(nx, x, ny, y, tg) 
        tg = add_X_after_Y_metagraph(nz, z, nx, x, tg) 
    return nx, tg
        



# add a relation in timegraph 
def add_relation_in_timegraph(X, Y, rel, tg, comment_n_weight): 
#    global count_relation 
    global count_not_handled
#    global count_node
#    global next_chain 
    global base_value 
    global diff 

    tg.count_relation += 1 

    rel = rel.upper() 
    handled = 'no' 
    x1 = X+'_s'
    x2 = X+'_e'    
    y1 = Y+'_s'
    y2 = Y+'_e'

    if X == Y and not (rel == 'IDENTITY' or rel == 'SIMULTANEOUS'):
        count_not_handled += 1 
        tg.violated_relations += name+'\t'+X+'\t'+Y+'\t'+rel+'\t'+comment_n_weight+'\n'
        handled = 'yes' 
        if debug_verification == 'true': 
            print 'closure violation:', X, Y, rel


    if y1 not in tg.node_array and y2 not in tg.node_array and x1 in tg.node_array and x2 in tg.node_array:
        if debug >= 2: 
            print 'replaced' 
        rel = reverse_relation(rel) 
        x1 = Y+'_s'
        x2 = Y+'_e'    
        y1 = X+'_s'
        y2 = X+'_e'
        
    # both of the elements doens't exist in the tg.node_array 
    if not (x1 in tg.node_array or x2 in tg.node_array or y1 in tg.node_array or y2 in tg.node_array):
        tg.count_node += 4 
        nx1 = Node(x1)
        nx2 = Node(x2)
        ny1 = Node(y1)
        ny2 = Node(y2)
        
        # assign current chain to all entities
        nx1.chain = tg.next_chain 
        nx2.chain = tg.next_chain 
        ny1.chain = tg.next_chain 
        ny2.chain = tg.next_chain 
        
        # increment current chain number
        tg.next_chain += 1 

        if debug >= 2: 
            print x1, x2, y1, y2, rel 
        if rel.upper() == 'BEFORE':
            handled = 'yes' 
            # x1 < x2 < y1 < y2 
            nx1.pseudo = base_value 
            nx1.parent = NIL 
            nx1.child = x2 
            nx2.pseudo = nx1.pseudo + diff 
            nx2.parent = x1 
            nx2.child = y1 
            ny1.pseudo = nx2.pseudo + diff 
            ny1.parent = x2 
            ny1.child = y2
            ny2.pseudo = ny1.pseudo + diff 
            ny2.parent = y1 
            ny2.child = NIL  

        if rel.upper() == 'AFTER':
            # y1 < y2 < x1 < x2
            handled = 'yes' 
            ny1.pseudo = base_value 
            ny1.parent = NIL 
            ny1.child = y2 
            ny2.pseudo = ny1.pseudo + diff 
            ny2.parent = y1 
            ny2.child = x1 
            nx1.pseudo = ny2.pseudo + diff 
            nx1.parent = y2 
            nx1.child = x2 
            nx2.pseudo = nx1.pseudo + diff 
            nx2.parent = x1
            nx2.child = NIL 

        if rel.upper() == 'IBEFORE': # Allen's MEET (m)
            # X m Y : x1 < y1, x1 < y2, x2 = y1, x2 < y2 : x1 < x2 = y1 < y2 
            handled = 'yes' 
            nx1.pseudo = base_value 
            nx1.parent = NIL 
            nx1.child = x2 + ' ' + y1  
            nx2.pseudo = nx1.pseudo + diff 
            nx2.parent = x1 
            nx2.child = y2
            nx2.sibling = y1 
            ny1.pseudo = nx2.pseudo 
            ny1.parent = x1 
            ny1.child = y2
            ny1.sibling = x2 
            ny2.pseudo = ny1.pseudo + diff 
            ny2.parent = y1 + ' ' + x2 
            ny2.child = NIL  

        if rel.upper() == 'IAFTER': # Allen's METBY (mi) 
            # X mi Y : x1 > y1, x1 = y2, x2 > y1, x2 > y2 : y1 < y2 = x1 < x2 
            handled = 'yes' 
            ny1.pseudo = base_value 
            ny1.parent = NIL 
            ny1.child = y2 + ' ' + x1 
            ny2.pseudo = ny1.pseudo + diff 
            ny2.parent = y1 
            ny2.child = x2
            ny2.sibling = x1 
            nx1.pseudo = ny2.pseudo 
            nx1.parent = y1 
            nx1.child = x2 
            nx1.sibling = y2 
            nx2.pseudo = nx1.pseudo + diff 
            nx2.parent = x1 + ' ' + y2
            nx2.child = NIL 

        if rel.upper() == 'DURING': # Allen's OVERLAP (o) 
            # X o Y : x1 < y1, x1 < y2, x2 > y1, x2 < y2 : x1 < y1 < x2 < y2  
            handled = 'yes' 
            nx1.pseudo = base_value 
            nx1.parent = NIL 
            nx1.child = y1 
            ny1.pseudo = nx1.pseudo + diff 
            ny1.parent = x1 
            ny1.child = x2
            nx2.pseudo = ny1.pseudo + diff 
            nx2.parent = y1 
            nx2.child = y2 
            ny2.pseudo = nx2.pseudo + diff 
            ny2.parent = x2 
            ny2.child = NIL  

        # Alen's OVERLAPBY is not covered in TimeML
            # X oi Y : x1 > y1, x1 < y2, x2 > y1, x2 > y2 : y1 < x1 < y2 < x2

        if rel.upper() == 'BEGINS': # Allen's START (s) 
            # X s Y : x1 = y1, x1 < y2, x2 > y1, x2 < y2 : x1 = y1 < x2 < y2 
            handled = 'yes' 
            nx1.pseudo = base_value 
            nx1.parent = NIL 
            nx1.child = x2 
            nx1.sibling = y1
            ny1.pseudo = nx1.pseudo
            ny1.parent = NIL 
            ny1.child = x2
            ny1.sibling = x1 
            nx2.pseudo = nx1.pseudo + diff 
            nx2.parent = x1 + ' ' + y1 
            nx2.child = y2 
            ny2.pseudo = nx2.pseudo + diff 
            ny2.parent = x2 
            ny2.child = NIL  

        if rel.upper() == 'BEGUN_BY': # Allen's StartedBy (si)
            # X si Y : x1 = y1, x1 < y2, x2 > y1, x2 > y2 : x1 = y1 < y2 < x2  
            handled = 'yes' 
            nx1.pseudo = base_value 
            nx1.parent = NIL 
            nx1.child = y2 
            nx1.sibling = y1
            ny1.pseudo = nx1.pseudo
            ny1.parent = NIL 
            ny1.child = y2
            ny1.sibling = x1 
            ny2.pseudo = nx1.pseudo + diff 
            ny2.parent = x1 + ' ' + y1 
            ny2.child = x2  
            nx2.pseudo = ny2.pseudo + diff 
            nx2.parent = y2
            nx2.child = NIL 
            
        if rel.upper() == 'ENDS': # Allen's Finish (f)
            # X f Y : x1 > y1, x1 < y2, x2 > y1, x2 = y2 : y1 < x1 < x2 = y2 
            handled = 'yes' 
            ny1.pseudo = base_value 
            ny1.parent = NIL 
            ny1.child = x1 
            nx1.pseudo = ny1.pseudo + diff 
            nx1.parent = y1 
            nx1.child = x2 + ' ' + y2 
            nx2.pseudo = nx1.pseudo + diff 
            nx2.parent = x1
            nx2.sibling = y2 
            nx2.child = NIL 
            ny2.pseudo = nx1.pseudo + diff 
            ny2.parent = x1 
            ny2.child = NIL 
            ny2.sibling = x2

        if rel.upper() == 'ENDED_BY': # Allen's FinishedBy (fi)
            # X fi Y : x1 < y1, x1 < y2, x2 > y1, x2 = y2 : x1 < y1 < y2 = x2 
            handled = 'yes' 
            nx1.pseudo = base_value 
            nx1.parent = NIL 
            nx1.child = y1
            ny1.pseudo = nx1.pseudo + diff 
            ny1.parent = x1 
            ny1.child = x2 + ' ' + y2 
            nx2.pseudo = ny1.pseudo + diff 
            nx2.parent = y1
            nx2.sibling = y2 
            nx2.child = NIL 
            ny2.pseudo = nx2.pseudo
            ny2.parent = y1 
            ny2.child = NIL 
            ny2.sibling = x2

        if rel.upper() == 'INCLUDES': # Allen's Contains (di) 
            # X di Y : x1 < y1, x1 < y2, x2 > y1, x2 > y2 : x1 < y1 < y2 < x2
            handled = 'yes' 
            nx1.pseudo = base_value 
            nx1.parent = NIL 
            nx1.child = y1 
            ny1.pseudo = nx1.pseudo + diff 
            ny1.parent = x1 
            ny1.child = y2
            ny2.pseudo = ny1.pseudo + diff 
            ny2.parent = y1 
            ny2.child = x2
            nx2.pseudo = ny2.pseudo + diff 
            nx2.parent = y2 
            nx2.child = NIL
            
        if rel.upper() == 'IS_INCLUDED': # Allen's During (d)
            # X d Y : x1 > y1, x1 < y2, x2 > y1, x2 < y2 : y1 < x1 < x2 < y2
            handled = 'yes' 
            ny1.pseudo = base_value 
            ny1.parent = NIL 
            ny1.child = x1 
            nx1.pseudo = ny1.pseudo + diff 
            nx1.parent = y1 
            nx1.child = x2 
            nx2.pseudo = nx1.pseudo + diff 
            nx2.parent = x1
            nx2.child = y2 
            ny2.pseudo = nx2.pseudo + diff 
            ny2.parent = x2 
            ny2.child = NIL 

        if rel.upper() == 'IDENTITY' or rel.upper() == 'SIMULTANEOUS': # Allen's Equality (=) 
            # X = Y : x1 = y1, x1 < y2, x2 > y1, x2 = y2 : x1 = y1 < x2 = y2
            handled = 'yes' 
            nx1.pseudo = base_value 
            nx1.parent = NIL 
            nx1.sibling = y1 
            nx1.child = x2 + ' ' + y2 
            ny1.pseudo = nx1.pseudo 
            ny1.parent = NIL
            ny1.sibling = x1 
            ny1.child = x2 + ' ' + y2
            nx2.pseudo = nx1.pseudo + diff 
            nx2.parent = x1 + ' ' + y1 
            nx2.sibling = y2 
            nx2.child = NIL 
            ny2.pseudo = nx2.pseudo
            ny2.parent = x1 + ' ' + y1 
            ny2.sibling = x2
            ny2.child = NIL  

        nx1.p = nx1.parent 
        nx1.c = nx1.child 
        nx2.p = nx2.parent 
        nx2.c = nx2.child 
        ny1.p = ny1.parent 
        ny1.c = ny1.child 
        ny2.p = ny2.parent 
        ny2.c = ny2.child 
        tg.node_array[x1] = nx1 
        tg.node_array[x2] = nx2 
        tg.node_array[y1] = ny1
        tg.node_array[y2] = ny2 


    # if first entity doesn't exist and second entity exists in the timegraph 
    elif (not x1 in tg.node_array) and (not x2 in tg.node_array) and y1 in tg.node_array and y2 in tg.node_array:
        tg.count_node += 2
        if rel.upper() == 'BEFORE':
            # X < Y : x1 < y1, x1 < y2, x2 < y1, x2 < y2 : x1 < x2 < y1 < y2 
            nx1 = Node(x1) 
            nx2 = Node(x2) 
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 

            ny1.c = ny1.c
            ny2.p = ny2.p 
            ny2.c = ny2.c 

            # if second entity doesn't have a parent then add in the same chain 
            if ny1.parent == NIL: 
                handled = 'yes' 
                nx1.chain = ny1.chain 
                nx2.chain = ny1.chain 
                nx1.pseudo = nx2.pseudo - diff 
                nx1.parent = NIL
                nx1.child = x2 
                nx2.pseudo = ny1.pseudo - diff 
                nx2.parent = x1 
                nx2.child = y1 
                ny1.parent = x2 
                
            else: 
                #create a new chain 
                handled = 'yes' 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.pseudo = nx2.pseudo - diff 
                nx1.parent = NIL 
                nx1.child = x2 
                nx2.pseudo = ny1.pseudo - diff 
                nx2.parent = x1 
                nx2.child = y1 
                ny1.parent += ' ' + x2 
                ## add metagraph information nx2, ny1
                tg = add_X_after_Y_metagraph(ny1, y1, nx2, x2, tg)
            nx1.p = nx1.parent 
            nx1.c = nx1.child 
            nx2.p = nx2.parent 
            nx2.c = nx2.child 
            ny1.p += ' ' + x2 

                
        if rel.upper() == 'AFTER':
            # X > Y : x1 > y1, x1 > y2, x2 > y1, x2 > y2 : y1 < y2 < x1 < x2 
            # if second entity doesn't have a child then add in the same chain 
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            nx1 = Node(x1) 
            nx2 = Node(x2) 
            handled = 'yes' 
            ny1.p = ny1.p
            ny1.c = ny1.c
            ny2.p = ny2.p

            if ny2.child == NIL: 
                nx1.chain = ny2.chain 
                nx1.pseudo = ny2.pseudo + diff 
                nx1.parent = y2 
                nx1.child = x2
                nx2.chain = ny2.chain 
                nx2.pseudo = nx1.pseudo + diff 
                nx2.parent = x1 
                nx2.child = NIL 
                ny2.child = x1 
            else: 
                # create a new chain 
                nx1.chain = tg.next_chain 
                nx1.pseudo = ny2.pseudo + diff 
                nx1.parent = y2 
                nx1.child = x2
                nx2.chain = tg.next_chain 
                nx2.pseudo = nx1.pseudo + diff 
                nx2.parent = x1 
                nx2.child = NIL 
                ny2.child += ' '+ x1 
                tg.next_chain += 1 
                # add metagraph information nx1, ny2 
                tg = add_X_after_Y_metagraph(nx1, x1, ny2, y2, tg)
            nx1.p = nx1.parent 
            nx1.c = nx1.child 
            nx2.p = nx2.parent 
            nx2.c = nx2.child 
            ny2.c += ' '+ x1 

        if rel.upper() == 'IBEFORE': # Allen's MEET (m)
            # X m Y : x1 < y1, x1 < y2, x2 = y1, x2 < y2 : x1 < x2 = y1 < y2 
            # if second entity doesn't have a parent then add in the same chain 
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            nx1 = Node(x1) 
            nx2 = Node(x2) 
            ny1.c = ny1.c
            ny2.c = ny2.c

            if ny1.parent == NIL: #  and re.search(y1, ny2.parent): 
                handled = 'yes' 
                nx1.chain = ny1.chain 
                nx2.chain = ny1.chain 
                ny1.parent = x1 
                ny1.sibling += ' '+ x2 
                ny2.parent += ' ' + x2 
                nx2.pseudo = ny1.pseudo
                nx2.parent = x1 
                nx2.child = ny1.child  
                nx2.sibling = ny1.sibling + ' ' + y1  
                nx1.pseudo = nx2.pseudo - diff 
                nx1.child = x2 + ' ' + y1 
                nx1.parent = NIL
            elif visualization == 'true': 
                handled = 'yes' 
                #create a new chain 
                nx2.chain = tg.next_chain 
                nx1.chain = tg.next_chain 
                nx1.parent = NIL 
                nx2.parent = x1 
                nx1.child = x2 
                nx2.child = NIL 
                nx2.pseudo = ny1.pseudo 
                nx1.pseudo = nx2.pseudo - diff 
                tg.next_chain += 1
                tg = add_X_after_Y_metagraph(nx2, x2, ny1, y1, tg) 
                tg = add_X_after_Y_metagraph(ny1, y1, nx2, x2, tg) 
            else: 
                handled = 'yes' 
                #create a new chain 
                nx1.chain = tg.next_chain 
                nx2.chain = ny1.chain 
                ny1.parent += ' ' + x1 
                ny1.sibling += ' ' + x2
                ny2.parent += ' ' + x2 
                nx2.pseudo = ny1.pseudo 
                nx2.parent = ny1.parent
                nx2.child = ny1.child 
                nx2.sibling = ny1.sibling + ' ' + y1 
                nx1.pseudo = nx2.pseudo - diff 
                nx1.child = x2 + ' ' + y1 
                nx1.parent = NIL 
#                nx2 = ny1
                tg.next_chain += 1
                tg = add_X_after_Y_metagraph(nx2, x2, nx1, x1, tg)
            nx1.p = nx1.parent 
            nx1.c = nx1.child 
            nx2.p = nx2.parent 
            nx2.c = nx2.child 
            ny1.p = ny1.parent 
            ny2.p += ' ' + x2 

 
        if rel.upper() == 'IAFTER': # Allen's METBY (mi) 
            # X mi Y : x1 > y1, x1 = y2, x2 > y1, x2 > y2 : y1 < y2 = x1 < x2 
            # if second entity doesn't have a child then add in the same chain 
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            handled = 'yes' 
            nx1 = Node(x1) 
            nx2 = Node(x2) 

            ny1.p = ny1.p
            ny2.p = ny2.p

            if ny2.child == NIL: #  and re.search(y1, ny2.parent): 
                ny1.child += ' ' + x1 
                ny2.sibling += ' ' + x1 
                ny2.child = x2 
                nx1.chain = ny2.chain 
                nx2.chain = ny2.chain 
                nx1.pseudo = ny2.pseudo 
                nx1.parent = ny2.parent 
                nx1.child = x2
                nx1.sibling = ny2.sibling + ' ' + y2 
                nx2.pseudo = nx1.pseudo + diff 
                nx2.parent = x1 + ' ' + y2 
                nx2.child = NIL 
            elif visualization == 'true': 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.parent = NIL 
                nx2.child = NIL 
                nx1.child = nx2 
                nx2.parent = nx1 
                nx1.pseudo = ny2.pseudo 
                nx2.pseudo = nx1.pseudo + diff 
                tg = add_X_after_Y_metagraph(nx1, x1, ny2, y2, tg) 
                tg = add_X_after_Y_metagraph(ny2, y2, nx1, x1, tg) 
            else: 
                # create a new chain 
                ny1.child += ' ' + x1 
                nx1.chain = tg.next_chain
                nx1 = ny2
                nx1.sibling += ' ' + y2 
                ny2.child += ' ' + x2 
                ny2.sibling += ' ' + x1 
                nx2.chain = tg.next_chain
                nx2.pseudo = ny2.pseudo + diff
                nx2.parent = x1 + ' ' + y2 
                nx2.child = NIL
                tg.next_chain += 1 
                tg = add_X_after_Y_metagraph(nx2, x2, nx1, x1, tg)
            nx1.p = nx1.parent 
            nx1.c = nx1.child 
            nx2.p = nx2.parent 
            nx2.c = nx2.child 
            ny1.c += ' ' + x1 
            ny2.c += ' ' + x2 

        if rel.upper() == 'DURING': # Allen's OVERLAP (o) 
            # X o Y : x1 < y1, x1 < y2, x2 > y1, x2 < y2 : x1 < y1 < x2 < y2  
            # can be handled doing point-wise instertion. insert y1 if x2 is the child of x1, check if x1 and x2 have siblings 
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            nx1 = Node(x1) 
            nx2 = Node(x2) 
            if ny1.parent == NIL and search_x_in_y(y2, ny1.child) and ny1.chain == ny2.chain: 
                # can add in the same chain 
                handled = 'yes' 
                nx1.chain = ny2.chain 
                nx2.chain = ny2.chain 
                nx1.parent = NIL 
                nx1.child = y1 
                nx1.pseudo = ny1.pseudo - diff 
                ny1.parent = x1 
                nx2.parent = y1
                nx2.child = ny1.child 
                nx2.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/2 
                ny1.child += ' ' + x2 # re.sub(y2, x2, ny1.child)

            elif (ny1.parent != NIL and not search_x_in_y(y2, ny1.child)) or visualization == 'true': 
                # same as visualization 
                handled = 'yes' 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.parent = NIL 
                nx1.pseudo = ny1.pseudo - diff 
                nx1.child = x2 
                nx2.parent = x1 
                nx2.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/2
                nx2.child = NIL 
                tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
                tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
                tg = add_X_after_Y_metagraph(nx2, x2, ny1, y1, tg) 
              
            else: # if visualization == 'false': # handle in timegraph manner, can insert start end in different chains 
                handled = 'yes'
                nx1, tg = add_point_x_BEFORE_y(x1, y1, tg) 
                nx2, tg = add_point_x_DURING_y_z(x2, y1, y2, tg) 
            nx1.p = nx1.parent 
            nx1.c = nx1.child 
            nx2.p = nx2.parent 
            nx2.c = nx2.child 
            ny1.p = ny1.parent 
            ny1.c = ny1.child 
            ny2.p = ny2.parent 
            ny2.c = ny2.child 
        
        if rel.upper() == 'DURING_INV': # Allen's Overlapped By, doesn't exist in TimeML, but our reverse will get it 
        # X oi Y : x1 > y1, x1 < y2, x2 > y1, x2 > y2 : y1 < x1 < y2 < x2
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            nx1 = Node(x1) 
            nx2 = Node(x2) 
            if ny2.child == NIL and search_x_in_y(y2, ny1.child) and ny1.chain == ny2.chain: 
                # can add in the same chain 
                nx1.chain = ny1.chain 
                nx2.chain = ny1.chain 
                handled = 'yes' 
                nx1.child = ny1.child 
                ny1.child += ' ' + x1 #  re.sub(y2, x1, ny1.child)
                nx1.parent = y1 
                nx1.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/2
                ny2.parent = x1 
                ny2.child = x2 
                nx2.parent = y2
                nx2.child = NIL 
                nx2.pseudo = ny2.pseudo + diff 
                
            elif (ny2.child == NIL and not search_x_in_y(y2, ny1.child)) or visualization == 'true': 
                # same as visualization 
                handled = 'yes' 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/2 
                nx2.pseudo = ny2.pseudo + diff 
                nx1.parent = y1 
                nx1.child = ny1.child 
                ny1.child = x1
                ny2.parent = x1 
                ny2.child = x2 
                nx2.parent = y2 
                ny2.child = NIL 
                tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
                tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
                tg = add_X_after_Y_metagraph(ny2, y2, nx1, x1, tg) 

            else: 
                handled = 'yes' 
                nx1, tg = add_point_x_DURING_y_z(x1, y1, y2, tg) 
                nx2, tg = add_point_x_AFTER_y(x2, y2, tg) 
            nx1.p = nx1.parent 
            nx1.c = nx1.child 
            nx2.p = nx2.parent 
            nx2.c = nx2.child 
            ny1.p = ny1.parent 
            ny1.c = ny1.child 
            ny2.p = ny2.parent 
            ny2.c = ny2.child 

        if rel.upper() == 'BEGINS': # Allen's START (s) 
            # X s Y : x1 = y1, x1 < y2, x2 > y1, x2 < y2 : x1 = y1 < x2 < y2 
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            handled = 'yes'
            nx1 = Node(x1) 
            nx2 = Node(x2) 

            nx1.p = ny1.p
            nx1.c = x2 
            nx2.p = x1 + ' ' + y1 
            nx2.c = y2 
            ny1.p = ny1.p
            ny1.c = ny1.c + ' ' + x2 
            ny2.p = ny2.p + ' ' + x2 
            ny2.c = ny2.c 

            if search_x_in_y(y2, ny1.child) and search_x_in_y(y1, ny2.parent) and ny1.chain == ny2.chain: 
                nx1.chain = ny1.chain 
                nx1 = ny1 
                nx1.sibling = ny1.sibling + ' ' + y1 
                nx2.chain = ny1.chain 
                nx2.parent = x1 + ' ' + y1 
                nx2.child = y2 
                ny1.sibling += ' ' + x1 
                ny1.child = re.sub(y2, x2, ny1.child) 
                nx2.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/2 
                ny2.parent = re.sub(y1, x2, ny2.parent) 
                
            elif visualization == 'true': 
                # same as visualization 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.pseudo = ny1.pseudo 
                nx2.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/2 
                tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
                tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
                tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
                nx1.parent = NIL 
                nx1.child = x2 
                nx2.parent = x1
                nx2.child = NIL 
            else: #  (not search_x_in_y(y2, ny1.child) or not search_x_in_y(y1, ny2.parent)) or
                nx2, tg = add_point_x_DURING_y_z(x2, y1, y2, tg) 
                ny1.sibling += ' ' + x1 
                nx1 = ny1 
                nx1.sibling += ' ' + y1 

        if rel.upper() == 'BEGUN_BY': # Allen's StartedBy (si)
            # X si Y : x1 = y1, x1 < y2, x2 > y1, x2 > y2 : x1 = y1 < y2 < x2  
            # handle point-wise 
            handled = 'yes'
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            nx1 = Node(x1) 
            nx2 = Node(x2) 
            
            nx1.p = ny1.p 
            nx1.c = ny1.c 
            nx2.p = y2 
            nx2.c = NIL 
            ny1.p = ny1.p 
            ny1.c = ny1.c 
            ny2.p = ny2.p 
            ny2.c += ' ' + x2 

            if ny2.child == NIL and ny1.chain == ny2.chain: 
                nx1.chain = ny1.chain 
                nx2.chain = ny1.chain 
                handled = 'yes' 
                nx2.pseudo = ny2.pseudo + diff 
                ny1.sibling += ' ' + x1 
                nx1 = ny1 
                nx1.sibling += ' ' + y1 
                ny2.parent += ' ' + x1 
                nx2.parent = y2 
                ny2.child = x2 
                nx2.child = NIL 

            elif visualization == 'true': # made it and because otherwise it won't go to else case
                handled = 'yes' 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
                tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
                nx1.parent = NIL 
                nx1.child = x2 
                nx1.pseudo = ny1.pseudo 
                nx2.parent = x1 
                nx2.child = NIL 
                nx2.pseudo = ny2.pseudo + diff 
                tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
            else:
                # ny2.child != NIL and 
                nx2, tg = add_point_x_AFTER_y(x2, y2, tg)  
                nx1 = ny1 

                
        if rel.upper() == 'ENDS': # Allen's Finish (f)
            # X f Y : x1 > y1, x1 < y2, x2 > y1, x2 = y2 : y1 < x1 < x2 = y2 
            handled = 'yes' 
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2]
            nx1 = Node(x1) 
            nx2 = Node(x2) 

            nx1.p = y1 
            nx1.c = x2 
            nx2.p = x1 
            nx2.c = ny2.c
            ny1.p = ny1.p 
            ny1.c += ' '+ x1 
            ny2.p = x2
            ny2.c = ny2.c

            if search_x_in_y(y2, ny1.child) and search_x_in_y(y1, ny2.parent) and ny1.chain == ny2.chain: 
                nx1.chain = ny1.chain 
                nx2.chain = ny1.chain 
                nx1.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/2 
                nx2.pseudo = ny2.pseudo 
                ny1.child = x1 # re.sub(y2, x1, ny1.child) 
                nx1.parent = y1 
                nx1.child = x2 
                nx2.parent = x1 
                nx2.sibling = ny2.sibling + ' ' + y2 
                ny2.sibling += ' ' + x2 
                nx2.child = ny2.child 
                ny2.parent = x1 

            elif visualization == 'true': 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/2 
                nx2.pseudo = ny2.pseudo 
                tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
                tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
                tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
            # (not search_x_in_y(y2, ny1.child) or not search_x_in_y(y1, ny2.parent)) or 
            else:
                nx2 = ny2 
                nx1, tg = add_point_x_DURING_y_z(x1, y1, y2, tg)
            
        if rel.upper() == 'ENDED_BY' : # Allen's finishedBy (fi) 
            # X fi Y : x1 < y1, x1 < y2, x2 > y1, x2 = y2 : x1 < y1 < y2 = x2 
            handled = 'yes' 
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            nx1 = Node(x1) 
            nx2 = Node(x2) 

            nx1.p = NIL 
            nx1.c = y1 
            nx2.p = ny2.p 
            nx2.c = ny2.c 
            ny1.p += ' ' + x1
            ny1.c = ny1.c + ' ' + x2 
            ny2.p = ny2.p
            ny2.c = ny2.c

            if ny1.parent == NIL and ny1.chain == ny2.chain: 
                nx1.chain = ny1.chain 
                nx2.chain = ny1.chain 
                nx1.pseudo = ny1.pseudo - diff 
                nx2.pseudo = ny2.pseudo 
                nx1.parent = NIL 
                nx1.child = y1 
                nx2.parent = ny2.parent 
                nx2.child = ny2.child 
                nx2.sibling = ny2.sibling + ' ' + y2 
                ny1.parent = x1 
                ny1.child += ' ' + x2 
                ny2.sibling += ' ' + x2 

            elif visualization == 'true': 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.pseudo = ny1.pseudo - diff 
                nx2.pseudo = ny2.pseudo 
                nx1.parent = NIL 
                nx1.child = x2 
                nx2.parent = x1 
                nx2.child = NIL 
                tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
                tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
                tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
            # ny1.parent != NIL or 
            else: 
                nx2 = ny2 
                nx1, tg = add_point_x_BEFORE_y(x1, y1, tg) 


        # todo: fix from here 
        if rel.upper() == 'INCLUDES': # Allen's contains (di)
            # X di Y : x1 < y1, x1 < y2, x2 > y1, x2 > y2 : x1 < y1 < y2 < x2 
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            handled = 'yes' 
            nx1 = Node(x1) 
            nx2 = Node(x2) 

            nx1.p = NIL
            nx1.c = y1 
            nx2.p = y2 
            nx2.c = NIL
            # ny1.p later 
            ny1.c = ny1.c 
            ny2.p = ny2.p 
            #ny2.c later


            if ny1.parent == NIL and ny2.child == NIL and ny1.chain == ny2.chain: 
                nx1.chain = ny1.chain 
                nx2.chain = ny2.chain 
                ny1.parent = x1 
                ny1.p += ' ' + x1 
                nx1.pseudo = ny1.pseudo - diff 
                nx1.parent = NIL 
                nx1.child = y1 
                ny2.child = x2 
                ny2.c += ' '+ x2 
                nx2.parent = y2 
                nx2.child = NIL 
                nx2.pseudo = ny2.pseudo + diff 

            elif ny1.parent != NIL or ny2.child != NIL or visualization == 'true':
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.pseudo = ny1.pseudo - diff 
                nx2.pseudo = ny2.pseudo + diff 
                nx1.parent = NIL 
                nx2.child = x2
                nx2.parent = x1 
                nx2.child = NIL 
                ny1.p += ' ' + x1 
                ny2.c += ' '+ x2 
                tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
                tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
            #
            else: 
                nx1, tg = add_point_x_BEFORE_y(x1, y1, tg)                 
                nx2, tg = add_point_x_AFTER_y(x2, y2, tg)                                 
                ny1.p = ny1.p + ' ' + x1 
                ny2.c = ny2.c + ' ' + x2 



        if rel.upper() == 'IS_INCLUDED': # Allen's During (d)
            # X d Y : x1 > y1, x1 < y2, x2 > y1, x2 < y2 : y1 < x1 < x2 < y2
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2] 
            nx1 = Node(x1) 
            nx2 = Node(x2) 
            handled = 'yes'

            nx1.p = ny1.sibling + ' ' + y1
            nx1.c = x2 
            nx2.p = x1 
            nx2.c = ny2.sibling + ' ' + y2 
            ny1.p = ny1.p 
            ny1.c = ny1.c + ' ' + x1 
            ny2.p = ny2.p + ' ' + x2 
            ny2.c = ny2.c

#            print x1, '#', nx1.p, '#', nx1.c 
#            print x2, '#', nx2.p, '#', nx2.c 
#            print y1, '#', ny1.p, '#', ny1.c 
#            print y2, '#', ny2.p, '#', ny2.c 
 
            if search_x_in_y(y2, ny1.child) and search_x_in_y(y1, ny2.parent) and ny1.chain == ny2.chain: 
                nx1.chain = ny1.chain 
                nx2.chain = ny1.chain
                nx1.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/3 
                nx2.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)*2/3 
                nx1.parent = ny1.sibling + ' ' + y1 
                nx1.p = ny1.sibling + ' ' + y1
                nx2.child = ny2.sibling + ' ' + y2 
                nx2.c = ny2.sibling + ' ' + y2 
                ny1.child = x1 
                nx1.child = x2
                nx2.parent = x1 
##            elif visualization == 'false' and ny1.child == NIL: 
##                nx1.chain = ny1.chain 
##                ny1.child = x1
##                nx1.parent = y1
##                nx1.child = x2
##                nx1.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/3 
##                nx2.parent = x1 
##                nx2.child = NIL 
##                nx2.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)*2/3 
##                nx2.chain = ny1.chain 
##                tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
##            elif visualization == 'false' and ny2.parent == NIL: 
##                ny2.parent = x2
##                nx2.child = y2 
##                nx2.parent = x1 
##                nx2.pseudo = ny2.pseudo - (ny2.pseudo - ny1.pseudo)/3 
##                nx1.parent = NIL 
##                nx1.child = x2
##                nx1.pseudo = ny2.pseudo - (ny2.pseudo - ny1.pseudo)*2/3 
##                nx1.chain = ny2.chain 
##                nx2.chain = ny2.chain 
##                tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
            else: ##if visualization == 'true': 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)/3
                nx2.pseudo = ny1.pseudo + (ny2.pseudo - ny1.pseudo)*2/3 
                nx1.parent = NIL 
                nx1.child = x2 
                nx2.parent = x1 
                nx2.child = NIL 
                tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
                tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
##            else:
                # (not search_x_in_y(y2, ny1.child) or not search_x_in_y(y1, ny2.parent)) or 
##                nx1, tg = add_point_x_DURING_y_z(x1, y1, y2, tg)
##                nx2, tg = add_point_x_DURING_y_z(x2, x1, y2, tg)
                

        if rel.upper() == 'IDENTITY' or rel.upper() == 'SIMULTANEOUS': # Allen's equality (=) 
            # X = Y : x1 = y1, x1 < y2, x2 > y1, x2 = y2 : x1 = y1 < x2 = y2
            ny1 = tg.node_array[y1]
            ny2 = tg.node_array[y2]
            handled = 'yes' 
            nx1 = Node(x1) 
            nx2 = Node(x2) 

            nx1.p = ny1.p 
            nx1.c = ny1.c + ' ' + x2 
            nx2.p = ny2.p + ' ' + x1 
            nx2.c = ny2.c
            ny1.p = ny1.p 
            ny1.c = ny1.c + ' ' + x2 
            ny2.p = ny2.p + ' ' + x1 
            ny2.c = ny2.c

            if ny1.chain == ny2.chain: 
                nx1 = ny1 
                nx2 = ny2 
                nx1.sibling += ' ' + y1 
                nx2.sibling += ' ' + y2 
                ny1.sibling += ' ' + x1 
                ny2.sibling += ' ' + x2 
            elif visualization == 'true': 
                handled = 'yes' 
                nx1.chain = tg.next_chain 
                nx2.chain = tg.next_chain 
                tg.next_chain += 1 
                nx1.parent = NIL 
                nx1.pseudo = ny1.pseudo 
                nx1.child = x2 
                nx2.parent = x1 
                nx2.pseudo = ny2.pseudo 
                nx2.child = NIL 
                tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
                tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
                tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
                tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
            else:
                # ny1.chain != ny2.chain or 
                nx1 = ny1 
                nx2 = ny2 

        if handled == 'yes':
            tg.node_array[x1] = nx1 
            tg.node_array[x2] = nx2 
            tg.node_array[y1] = ny1
            tg.node_array[y2] = ny2 


    # if first entity exists and second entity doesn't exist in the timegraph 
            
    # if both entities exists 
        # consistency checking or merging chains 
        # if in the same chain then consistency checking 
        # if in different chains then merging chains or adding cross chains in metagraph 
        # check consistency 
        # if non-consistent then remove 
        # if consistent then remove from annotation!
        # if no relation then add metagraph 
        # end result: do nothing! 
        # if different chains then connect them through cross chain 
    if handled == 'no':
        if debug >= 1: 
            print 'add new rel', X, Y, rel
        tg, match = interval_rel_X_Y(X, Y, tg, rel, 'check_n_merge') 
        if match == 'false':
            tg.violated_relations += name+'\t'+X+'\t'+Y+'\t'+rel+'\t'+comment_n_weight+'\n'
            count_not_handled += 1 
            if debug >= 1: 
                print 'didn\'t add. closure violation:', X, Y, rel
            if debug_verification == 'true': 
                print 'closure violation:', X, Y, rel
            return match 
        if match == 'true': 
            ## todo: the relation is verified and not added 
            todo = 'add relations that need to be removed from reduced graph' 
            if debug >= 1: 
                print X, Y, rel, todo 
            tg.remove_from_reduce += name+'\t'+X+'\t'+Y+'\t'+rel+'\t'+comment_n_weight+'\n'

        if match == 'semi-true': 
            ## todo: these were UNKNOWN relations and we added these relations newly 
            todo = 'new relations added' 
            if debug >= 1: 
                print X, Y, rel, todo 
    

    if handled == 'yes': 
        if debug >= 2: 
            # print the pseudo time 
            print str(tg.node_array[x1].pseudo)+'-'+str(tg.node_array[x1].chain), str(tg.node_array[x2].pseudo)+'-'+str(tg.node_array[x2].chain), str(tg.node_array[y1].pseudo)+'-'+str(tg.node_array[y1].chain), str(tg.node_array[y2].pseudo)+'-'+str(tg.node_array[y2].chain)
            if tg.node_array[x2].chain != tg.node_array[y1].chain or tg.node_array[x1].chain != tg.node_array[y2].chain :
                if tg.node_array[x1].chain in tg.metagraph:
                    print 'x1', x1
                    print 'connection points', tg.metagraph[tg.node_array[x1].chain].cp
                    all_cp = tg.metagraph[tg.node_array[x1].chain].cp
                    for tmp_cp in all_cp.split(' '): 
                        print 'for cp', tmp_cp
##                        print 'cp to accessible chains', tg.metagraph[tg.node_array[x1].chain].cp_to_chain[tmp_cp] 
                        print 'cross chain', tg.metagraph[tg.node_array[x1].chain].cross_chain[tmp_cp] 
                if tg.node_array[x2].chain in tg.metagraph: 
                    print 'x2', x2
##                    print 'connection points', tg.metagraph[tg.node_array[x2].chain].cp
                    all_cp = tg.metagraph[tg.node_array[x2].chain].cp
                    for tmp_cp in all_cp.split(' '): 
#                        print tg.node_array[x2].pseudo 
#                        print tg.node_array[tmp_cp].pseudo 
                        print 'for cp', tmp_cp
#                        print 'cp to accessible chains', tg.metagraph[tg.node_array[x2].chain].cp_to_chain[tmp_cp] 
                        print 'cross chain', tg.metagraph[tg.node_array[x2].chain].cross_chain[tmp_cp] 

                if tg.node_array[y1].chain in tg.metagraph: 
                    print 'y1', y1
                    print 'connection points', tg.metagraph[tg.node_array[y1].chain].cp
                    all_cp = tg.metagraph[tg.node_array[y1].chain].cp
                    for tmp_cp in all_cp.split(' '): 
                        print 'for cp', tmp_cp
##                        print 'cp to accessible chains', tg.metagraph[tg.node_array[y1].chain].cp_to_chain[tmp_cp] 
                        print 'cross chain', tg.metagraph[tg.node_array[y1].chain].cross_chain[tmp_cp] 

                if tg.node_array[y2].chain in tg.metagraph: 
                    print 'y2', y2
                    print 'connection points', tg.metagraph[tg.node_array[y2].chain].cp
                    all_cp = tg.metagraph[tg.node_array[y2].chain].cp
                    for tmp_cp in all_cp.split(' '): 
                        print 'for cp', tmp_cp
##                        print 'cp to accessible chains', tg.metagraph[tg.node_array[y2].chain].cp_to_chain[tmp_cp] 
                        print 'cross chain', tg.metagraph[tg.node_array[y2].chain].cross_chain[tmp_cp] 



        

def get_entities_add_relation_in_timegraph(line, tg): 
    each = line
#    global entity_to_lines 
#    global filetext_queue 
#    global q
#    global once_in_queue
#    global entries_added 
    words = each.split('\t')
    # if the TLINK line (each) doesn't exist in entries_added (i.e. its not been added before) then add in Timegraph 
    # add relation in Timegraph 
    if not re.search(each, tg.entries_added): 
        if debug >= 2: 
            print 'add ', words[1], words[2], 'with relation', words[3], 'in Timegraph' 
        comment_n_weight = '' 
        for i in range(4, len(words)): 
            comment_n_weight += words[i]+'\t' 
        match = add_relation_in_timegraph(words[1], words[2], words[3], tg, comment_n_weight)

        if match == 'false': 
            if debug >= 1: 
                foo = line.split('\t')[5]
                if int(foo)> 97: 
                    print 'violation', line 

    else: 
        if debug >= 2: 
            print 'already added', each 

    # add TLINK line in entries_added to make sure we don't add the same relation in the Timegraph again                     
    tg.entries_added += '\n'+each
    # if not in once_in_queue then add the entries in once_in_queue 
    if not search_x_in_y(words[1], tg.once_in_queue): 
        if debug >= 2: 
            print 'added next entity', words[1]
        tg.q.put(words[1]) 
        tg.once_in_queue += ' ' + words[1]

    if not search_x_in_y(words[2], tg.once_in_queue): 
        if debug >= 2:
            print 'added next entity', words[2]
        tg.q.put(words[2]) 
        tg.once_in_queue += ' ' + words[2] 


## reads all the TLINKs 
def read_tlinks(tlink_file): 
    filetext = '' 
    if tlink_files == 'multiple_files':
        for each in order.split('>'): 
            prename = basedir + '2-tempeval-format/'
            postname = '/'+name+'.txt'
            filename = prename+each+postname
            filetext += open(filename).read() 
    else: # tlink_files == 'one_file': 
#        filetext = open(all_tlinks).read() 
        filetext = open(tlink_file).read() 

    return filetext 

    
def find_point_rel(a, b): 
    if a > b: 
        return '>' 
    if a < b: 
        return '<' 
    if a == b: 
        return '=' 
    return 'UNKNOWN' 


# traverse through timegraph to identify relation between two points 
def traverse_timegraph_identify_rel(nx, x, ny, y, tg, chain_history):
    if debug >= 1: 
        print x, y, nx.chain, ny.chain, nx.pseudo, ny.pseudo
    if nx.chain == ny.chain and nx.pseudo <= ny.pseudo: 
        return 'true' 
    elif nx.chain == ny.chain: 
        return 
    chain_history += ' ' + str(nx.chain)
    if debug >= 3: 
        print chain_history 
    if nx.chain in tg.metagraph: 
        all_cp = tg.metagraph[nx.chain].cp
        for cp in all_cp.split(' '):
            ncp = tg.node_array[cp]
            if debug >= 1: 
                print 'x:', x
                print 'x pseudo', nx.pseudo
                print 'cp psuedo', ncp.pseudo
                print 'tg.metagraph[nx.chain].cross_chain[cp]',  tg.metagraph[nx.chain].cross_chain[cp]
            if int(nx.pseudo) <= int(ncp.pseudo):
                if debug >= 1:
                    print 'debug: nx', nx.pseudo, 'ncp', ncp.pseudo 
                # explore chain, if there is a path then x <=y
                for each in tg.metagraph[nx.chain].cross_chain[cp].split(' '):
                    each_crosschain = tg.node_array[each]
                    if ignore_chain_history == 'true' or not re.search(' '+str(each_crosschain.chain)+' ', ' '+chain_history+' '): 
                        if debug >= 1: 
                            print 'cp in crosschain', each
                            print 'chain in crosschain', each_crosschain.chain 
                        ### becomes a problem for cases when we add new crosschain afterwards
##                        if ncp.pseudo <= each_crosschain.pseudo:
                        foo = traverse_timegraph_identify_rel(each_crosschain, each, ny, y, tg, chain_history)
                        if foo == 'true': 
                            return foo 


# find relation in timegraph
def fine_relation_in_timegraph(nx, x, ny, y, tg): 
    chain_history = '' 
    x_to_y = traverse_timegraph_identify_rel(nx, x, ny, y, tg, chain_history)
    if debug >= 1: 
        print 'x_to_y', x_to_y
    chain_history = '' 
    y_to_x = traverse_timegraph_identify_rel(ny, y, nx, x, tg, chain_history)
    if debug >= 1: 
        print 'y_to_x', y_to_x
    if x_to_y  == 'true' and y_to_x == 'true': 
        return '=' 
    if x_to_y == 'true': 
        return '<'
    if y_to_x == 'true': 
        return '>' 
    return 'UNKNOWN' 

# find the relationship between two time points in a timegraph 
def point_rel_x_y(nx, x, ny, y, tg): 
    if debug >= 1: 
        print x, y
        print nx.chain, ny.chain
        print nx.pseudo, ny.pseudo
    if nx.chain == ny.chain: 
        foo = find_point_rel(nx.pseudo, ny.pseudo) 
        if debug >= 1: 
            print foo
        return foo 
    else: 
        foo = fine_relation_in_timegraph(nx, x, ny, y, tg)
        if debug >= 1: 
            print foo
        return foo 
    
    return 'UNKNOWN' 


# add new relation between entities existing in timegraph 
def add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg): 
    nx1 = tg.node_array[x1]
    nx2 = tg.node_array[x2]
    ny1 = tg.node_array[y1]
    ny2 = tg.node_array[y2]

    if corpus_rel == 'BEFORE':
        tg = add_X_after_Y_metagraph(ny1, y1, nx2, x2, tg)
        nx2.c += ' ' + y1 
        ny1.p += ' ' + x2 

    if corpus_rel == 'AFTER':
        tg = add_X_after_Y_metagraph(nx1, x1, ny2, y2, tg)
        ny2.c += ' ' + x1 
        nx1.p += ' ' + y2 
        
    if corpus_rel == 'IBEFORE': 
        tg = add_X_after_Y_metagraph(nx2, x2, ny1, y1, tg) 
        ny1.c += ' ' + x2
        nx2.p += ' ' + y1 
        tg = add_X_after_Y_metagraph(ny1, y1, nx2, x2, tg) 
        nx2.c += ' ' + y1 
        ny1.p += ' ' + x2 
        nx2.sibling += ' ' + y1
        ny1.sibling += ' ' + x2     

    if corpus_rel == 'IAFTER':
        tg = add_X_after_Y_metagraph(nx1, x1, ny2, y2, tg) 
        ny2.c += ' ' + x1 
        nx1.p += ' ' + y2 
        tg = add_X_after_Y_metagraph(ny2, y2, nx1, x1, tg) 
        nx1.c += ' ' + y2 
        ny2.p += ' ' + x1 
        nx1.sibling += ' ' + y2 
        ny2.sibling += ' ' + x1 

    if corpus_rel == 'DURING':
        tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
        nx1.c += ' ' + y1 
        ny1.p += ' ' + x1 
        tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
        nx2.c += ' ' + y2 
        ny2.p += ' ' + x2 
        tg = add_X_after_Y_metagraph(nx2, x2, ny1, y1, tg) 
        ny1.c += ' ' + x2
        nx2.p += ' ' + y1 

    if corpus_rel == 'DURING_INV': 
        tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
        ny1.c += ' ' + x1
        nx1.p += ' ' + y1
        tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
        ny2.c += ' ' + x2 
        nx2.p += ' ' + y2 
        tg = add_X_after_Y_metagraph(ny2, y2, nx1, x1, tg) 
        nx1.c += ' ' + y2 
        ny2.p += ' ' + x1 

    if corpus_rel == 'BEGINS': 
        tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
        ny1.c += ' ' + x1
        nx1.p += ' ' + y1
        tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
        nx1.c += ' ' + y1
        ny1.p += ' ' + x1
        nx1.sibling += ' ' + y1 
        ny1.sibling += ' ' + x1 
        tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
        nx2.c += ' ' + y2 
        ny2.p += ' ' + x2

    if corpus_rel == 'BEGUN_BY': 
        tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
        ny1.c += ' ' + x1
        nx1.p += ' ' + y1
        tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
        nx1.c += ' ' + y1 
        ny1.p += ' ' + x1 
        nx1.sibling += ' ' + y1 
        ny1.sibling += ' ' + x1 
        tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
        ny2.c += ' ' + x2 
        nx2.p += ' ' + y2 

    if corpus_rel == 'ENDS': 
        tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
        ny1.c += ' ' + x1 
        nx1.p += ' ' + y1
        tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
        ny2.c += ' ' + x2
        nx2.p += ' ' + y2
        tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
        nx2.c += ' ' + y2 
        ny2.p += ' ' + x2
        nx2.sibling += ' ' + y2 
        ny2.sibling += ' ' + x2 


    if corpus_rel == 'ENDED_BY': 
        tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
        nx1.c += ' ' + y1
        ny1.p += ' ' + x1 
        tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
        nx2.c += ' ' + y2 
        ny2.p += ' ' + x2
        tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
        ny2.c += ' ' + x2
        nx2.p += ' ' + y2
        nx2.sibling += ' ' + y2 
        ny2.sibling += ' ' + x2 

    if corpus_rel == 'INCLUDES': 
        if debug >= 1: 
            print 'y1>x1', y1, x1 
        tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
        nx1.c += ' ' + y1
        ny1.p += ' ' + x1 
        if debug >= 1: 
            print 'x2>y2', x2, y2 
        tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
        ny2.c += ' ' + x2 
        nx2.p += ' ' + y2
        
    if corpus_rel == 'IS_INCLUDED': 
        tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
        ny1.c += ' ' + x1
        nx1.p += ' ' + y1
        tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg) 
        nx2.c += ' ' + y2 
        ny2.p += ' ' + x2
        
    if corpus_rel == 'IDENTITY' or corpus_rel == 'SIMULTANEOUS': 
        tg = add_X_after_Y_metagraph(nx1, x1, ny1, y1, tg) 
        ny1.c += ' ' + x1 
        nx1.p += ' ' + y1
        tg = add_X_after_Y_metagraph(ny1, y1, nx1, x1, tg) 
        nx1.c += ' ' + y1
        ny1.p += ' ' + x1 
        nx1.sibling += ' ' + y1 
        ny1.sibling += ' ' + x1 
        tg = add_X_after_Y_metagraph(nx2, x2, ny2, y2, tg) 
        ny2.c += ' ' + x2 
        nx2.p += ' ' + y2
        tg = add_X_after_Y_metagraph(ny2, y2, nx2, x2, tg)         
        nx2.c += ' ' + y2 
        ny2.p += ' ' + x2
        nx2.sibling += ' ' + y2 
        ny2.sibling += ' ' + x2 

    return tg 
        
# find relationship between two intervals 
# option = 'check_n_merge' OR 'evaluation' 
def interval_rel_X_Y(X, Y, tg, corpus_rel, option): 
    global count_didnt_match_rel
    x1 = X+'_s'
    x2 = X+'_e'    
    y1 = Y+'_s'
    y2 = Y+'_e'
    if not (x1 in tg.node_array and x2 in tg.node_array and y1 in tg.node_array and y2 in tg.node_array):
        if option == 'check_n_merge': 
            return tg, 'false' 
        else: 
            return tg, 'UNKNOWN' 

    nx1 = tg.node_array[x1]
    nx2 = tg.node_array[x2]
    ny1 = tg.node_array[y1]
    ny2 = tg.node_array[y2]
    # if any one of these are UNKNOWN then add new relations
    # if FOUND a relation then return and compare 
    if corpus_rel == 'BEFORE': 
        x2_y1 = point_rel_x_y(nx2, x2, ny1, y1, tg) 
        if x2_y1 == '<': 
            if debug >= 2: 
                print 'relation matched' 
            return tg, 'true'
        # if check_n_merge then merge and then return true
        # if queried for temporal evaluation, then if doesn't match then return false in the end 
        elif x2_y1 == 'UNKNOWN' and option == 'check_n_merge':
            tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)
            return tg, 'semi-true'
        else:
            if x2_y1 == 'UNKNOWN': 
                return tg, 'UNKNOWN' 
            count_didnt_match_rel += 1

    if corpus_rel == 'AFTER': 
        y2_x1 = point_rel_x_y(ny2, y2, nx1, x1, tg) 
        if y2_x1 == '<': 
            if debug >= 2: 
                print 'relation matched' 
            return tg, 'true'
        elif y2_x1 == 'UNKNOWN' and option == 'check_n_merge':
            tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)  
            return tg, 'semi-true'
        else:
            if y2_x1 == 'UNKNOWN': 
                return tg, 'UNKNOWN' 
            count_didnt_match_rel += 1

    if corpus_rel == 'IBEFORE': 
        x2_y1 = point_rel_x_y(nx2, x2, ny1, y1, tg) 
        if x2_y1 == '=': 
            if debug >= 2: 
                print 'relation matched' 
            return tg, 'true'
        elif x2_y1 == 'UNKNOWN' and option == 'check_n_merge':
            tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)                   
            return tg, 'semi-true'
        else:
            if x2_y1 == 'UNKNOWN': 
                return tg, 'UNKNOWN' 
            count_didnt_match_rel += 1

    if corpus_rel == 'IAFTER': 
        y2_x1 = point_rel_x_y(ny2, y2, nx1, x1, tg) 
        if y2_x1 == '=': 
            if debug >= 2: 
                print 'relation matched' 
            return tg, 'true'
        elif y2_x1 == 'UNKNOWN' and option == 'check_n_merge':
            tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)       
            return tg, 'semi-true'
        else:
            if y2_x1 == 'UNKNOWN': 
                return tg, 'UNKNOWN' 
            count_didnt_match_rel += 1

    if corpus_rel == 'DURING': 
        x1_y1 = point_rel_x_y(nx1, x1, ny1, y1, tg) 
        if x1_y1 == '<': 
            x2_y2 = point_rel_x_y(nx2, x2, ny2, y2, tg) 
            if x2_y2 == '<': 
                y1_x2 = point_rel_x_y(ny1, y1, nx2, x2, tg) 
                if y1_x2 == '<': 
                    if debug >= 2: 
                        print 'relation matched' 
                    return tg, 'true'
                elif option == 'check_n_merge': #elif y1_x2 == 'UNKNOWN' and x2_y2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN' and option == 'check_n_merge':
                    tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)                    
                    return tg, 'semi-true'
                else:
                    if y1_x2 == 'UNKNOWN' and x2_y2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN': 
                        return tg, 'UNKNOWN' 
                    count_didnt_match_rel += 1
            else:
                count_didnt_match_rel += 1
        else:
            count_didnt_match_rel += 1
            
    if corpus_rel == 'BEGINS': 
        x1_y1 = point_rel_x_y(nx1, x1, ny1, y1, tg) 
        if x1_y1 == '=' and x1_y1 == 'UNKNOWN': 
            x2_y2 = point_rel_x_y(nx2, x2, ny2, y2, tg) 
            if x2_y2 == '<' and x2_y2 == 'UNKNOWN': 
                if debug >= 2: 
                    print 'relation matched' 
                if x1_y1 == '=' and x2_y2 == '<': 
                    return tg, 'true'
                elif option == 'check_n_merge': #elif x2_y2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN' and option == 'check_n_merge':
                    tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)
                    return tg, 'semi-true'
            else:
                if x2_y2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN': 
                    return tg, 'UNKNOWN' 
                count_didnt_match_rel += 1
        else:
            count_didnt_match_rel += 1

    if corpus_rel == 'BEGUN_BY': 
        x1_y1 = point_rel_x_y(nx1, x1, ny1, y1, tg) 
        if x1_y1 == '=' or x1_y1 == 'UNKNOWN': 
            y2_x2 = point_rel_x_y(ny2, y2, nx2, x2, tg) 
            if y2_x2 == '<' or y2_x2 == 'UNKNOWN':  
                if debug >= 2: 
                    print 'relation matched' 
                if y2_x2 == '<' and x1_y1 == '=': 
                    return tg, 'true'
                elif option == 'check_n_merge': #elif y2_x2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN' and option == 'check_n_merge':
                    tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)
                    return tg, 'semi-true'
            else:
                if y2_x2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN': 
                    return tg, 'UNKNOWN'        
                count_didnt_match_rel += 1
        else:
            count_didnt_match_rel += 1

    if corpus_rel == 'ENDS': 
        x2_y2 = point_rel_x_y(nx2, x2, ny2, y2, tg) 
        if x2_y2 == '=' or x2_y2 == 'UNKNOWN': 
            y1_x1 = point_rel_x_y(ny1, y1, nx1, x1, tg) 
            if y1_x1 == '<' or y1_x1 == 'UNKNOWN': 
                if debug >= 2: 
                    print 'relation matched' 
                if x2_y2 == '=' and y1_x1 == '<': 
                    return tg, 'true'
                elif option == 'check_n_merge': #elif y1_x1 == 'UNKNOWN' and x2_y2 == 'UNKNOWN' and option == 'check_n_merge':
                    tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)
                    return tg, 'semi-true'
            else:
                if y1_x1 == 'UNKNOWN' and x2_y2 == 'UNKNOWN': 
                    return tg, 'UNKNOWN'        
                count_didnt_match_rel += 1
        else:
            count_didnt_match_rel += 1

    if corpus_rel == 'ENDED_BY': 
        x2_y2 = point_rel_x_y(nx2, x2, ny2, y2, tg) 
        if x2_y2 == '=' or x2_y2 == 'UNKNOWN': 
            x1_y1 = point_rel_x_y(nx1, x1, ny1, y1, tg) 
            if x1_y1 == '<' or x1_y1 == 'UNKNOWN': 
                if debug >= 2: 
                    print 'relation matched' 
                if x2_y2 == '=' and x1_y1 == '<':  
                    return tg, 'true'
                elif option == 'check_n_merge': #elif x1_y1 == 'UNKNOWN' and x2_y2 == 'UNKNOWN' and option == 'check_n_merge':
                    tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)
                    return tg, 'semi-true'
            else:
                if x1_y1 == 'UNKNOWN' and x2_y2 == 'UNKNOWN': 
                    return tg, 'UNKNOWN'        
                count_didnt_match_rel += 1
        else:
            count_didnt_match_rel += 1

    if corpus_rel == 'INCLUDES': 
        x1_y1 = point_rel_x_y(nx1, x1, ny1, y1, tg) 
        if x1_y1 == '<' or x1_y1 == 'UNKNOWN': 
            y2_x2 = point_rel_x_y(ny2, y2, nx2, x2, tg) 
            if y2_x2 == '<' or y2_x2 == 'UNKNOWN': 
                if debug >= 2: 
                    print 'relation matched' 
                if x1_y1 == '<' and y2_x2 == '<':  
                    return tg, 'true'
                elif option == 'check_n_merge': #elif y2_x2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN' and option == 'check_n_merge':
                    tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)
                    return tg, 'semi-true'
            else:
                if y2_x2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN': 
                    return tg, 'UNKNOWN'        
                count_didnt_match_rel += 1
        else:
            count_didnt_match_rel += 1


    if corpus_rel == 'IS_INCLUDED': 
        y1_x1 = point_rel_x_y(ny1, y1, nx1, x1, tg) 
        if y1_x1 == '<' or y1_x1 == 'UNKNOWN': 
            x2_y2 = point_rel_x_y(nx2, x2, ny2, y2, tg) 
            if x2_y2 == '<' or x2_y2 == 'UNKNOWN': 
                if debug >= 2: 
                    print 'relation matched' 
                if y1_x1 == '<' and x2_y2 == '<': 
                    return tg, 'true'
                elif option == 'check_n_merge': #elif x2_y2 == 'UNKNOWN' and y1_x1 == 'UNKNOWN' and option == 'check_n_merge':
                    tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)
                    return tg, 'semi-true'
            else:
                if x2_y2 == 'UNKNOWN' and y1_x1 == 'UNKNOWN': 
                    return tg, 'UNKNOWN'        
                count_didnt_match_rel += 1
        else:
            count_didnt_match_rel += 1

    if corpus_rel == 'IDENTITY' or corpus_rel == 'SIMULTANEOUS': 
        x1_y1 = point_rel_x_y(nx1, x1, ny1, y1, tg) 
        if x1_y1 == '=' or x1_y1 == 'UNKNOWN': 
            x2_y2 = point_rel_x_y(nx2, x2, ny2, y2, tg)             
            if x2_y2 == '=' or x2_y2 == 'UNKNOWN': 
                if debug >= 2: 
                    print 'relation matched' 
                if x1_y1 == '=' and x2_y2 == '=': 
                    return tg, 'true'
                elif option == 'check_n_merge': #elif x2_y2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN' and option == 'check_n_merge':
                    tg = add_relation_for_existing_entities_in_timegraph(x1, x2, y1, y2, corpus_rel, tg)
                    return tg, 'semi-true'
            else:
                if x2_y2 == 'UNKNOWN' and x1_y1 == 'UNKNOWN' : 
                    return tg, 'UNKNOWN'        
                count_didnt_match_rel += 1
                if debug >= 2: 
                    print 'x2_y2', x2_y2
        else:
            count_didnt_match_rel += 1
            if debug >= 2: 
                print 'x1_y1', x1_y1

    return tg, 'false' 


def change_DURING_relation(filetext): 
    newtext = '' 
    for line in filetext.split('\n'): 
        foo = '' 
        words = line.split('\t') 
        for i in range(0, len(words)): 
            if i == 3 and (words[i] == 'DURING' or words[i] == 'DURING_INV'): 
                foo += re.sub('DURING', 'SIMULTANEOUS', re.sub('DURING_INV', 'SIMULTANEOUS', words[i])) + '\t'
            else:
                foo += words[i] + '\t' 
        newtext += foo.strip() + '\n' 
    return newtext 

def create_timegraph_from_weight_sorted_relations(filetext, tg): 
	global init_dct
	global consider_neighbor
	handled_relation = '' 
	if consider_DURING_as_SIMULTANEOUS == True: 
		filetext = change_DURING_relation(filetext)
	for line in filetext.split('\n'): 
		if line.strip() == '': 
			continue 
		if debug >= 2: 
			print line 
		words = line.split('\t') 
		comment_n_weight = '' 
		for i in range(4, len(words)): 
			comment_n_weight += words[i]+'\t'
		handle = name+'\t'+words[1]+'\t'+words[2]+'\t'+words[3]
		if not re.search(handle, handled_relation): 
			get_entities_add_relation_in_timegraph(line, tg)
			handled_relation += handle +'\t'+comment_n_weight + '\n'
#            print handle +'\t'+comment_n_weight

	tg.nonredundant = handled_relation 

	for line in tg.nonredundant.split('\n'): 
		if not (re.search(line, tg.remove_from_reduce) or re.search(line, tg.violated_relations)): 
			tg.final_relations += line + '\n' 


	if debug >= 2: 
		print 'Non Redundant Relations' 
		print tg.nonredundant
		print 'Remove from Reduce graph'
		print tg.remove_from_reduce
		print 'Violated Closure Properties'
		print tg.violated_relations
		print 'Final Relations' 
		print tg.final_relations 

	return tg 


# create a timegraph from timeml annotation 
# NOT USING IT ANYMORE 
def tempeval_to_timegraph_func(filetext, dct_tid, tg): 
#    global entity_to_lines 
#    global filetext_queue 
#    global q
#    global once_in_queue
#    global entries_added 
    global init_dct
    global consider_neighbor

    ### add all lines corresponding to an entity 
    ### create a hash(entity, TLINKs including that entity) 
    for line in filetext.split('\n'): 
        if line.strip() == '': 
            continue 
        tg.filetext_queue.put(line)
        words = line.split('\t') 
        if debug >= 3: 
            print words[1], words[2], words[3]
        # add first entity's related TLINKs in the hash 
        if words[1] in tg.entity_to_lines: 
            tg.entity_to_lines[words[1]] += '\n'+line 
        else: 
            tg.entity_to_lines[words[1]] = line 

        # add second entity's related TLINKs in the hash 
        if words[2] in tg.entity_to_lines: 
            tg.entity_to_lines[words[2]] += '\n'+line 
        else:
            tg.entity_to_lines[words[2]] = line

    if not dct_tid in tg.entity_to_lines:
        tg.entity_to_lines[dct_tid] = ""

    if init_dct == 'true': 
        tg.q.put(dct_tid) 
    while 1:
        # if the neighbor queue is empty consider the line from filetext (all TLINK list) 
        if consider_neighbor=='false' or tg.q.qsize() == 0: 
            if tg.filetext_queue.qsize() > 0: 
                if debug >= 4: 
                    print 'ki mia', line
                line = tg.filetext_queue.get() 
                while re.search(line, tg.entries_added): 
                    if debug >= 2: 
                        print line, 'added already'
                    if tg.filetext_queue.qsize() > 0: 
                        line = tg.filetext_queue.get() 
                    else: 
                        break 
                if debug >= 2: 
                    print 'add now', line
                get_entities_add_relation_in_timegraph(line, tg)
            else: 
                break
                    
        else: 
            current_entity = tg.q.get() 
            # add current entity in once_in_queue 
            tg.once_in_queue += ' ' + current_entity 

            # add neighbors in the lists 
            for each in tg.entity_to_lines[current_entity].split('\n'): 
                if each.strip() == "": 
                    continue 
                if debug >= 2: 
                    print each 
                get_entities_add_relation_in_timegraph(each, tg)

        if tg.q.qsize() == 0 and tg.filetext_queue.qsize() == 0: 
            break

    for each in tg.entity_to_lines: 
        if debug >= 4: 
            print '####'
            print each
            print tg.entity_to_lines[each] 
    return tg 

## debug purporse 
from_this_file = 'false' 
if from_this_file=='true':
    filetext = read_tlinks(bar(1))
    start_time = time.time()
    tg = Timegraph() 
    tg = create_timegraph_from_weight_sorted_relations(filetext, tg) 
    end_time = time.time()
    if debug >= 0: 
        print '\nTime taken to construct timegraph' 
    if debug >= 2: 
        print len(filetext.split('\n')), ',', end_time-start_time 
    if debug >= 2:
        print count_not_handled, ',', len(filetext.split('\n'))
    if debug >= 2: 
        print 'count_didnt_match_rel ,', count_didnt_match_rel
    if debug >= 0: 
        print 'time, count_relation, nodes, edges'
    if debug >= 0: 
        print end_time-start_time, ',', tg.count_relation, ',', tg.count_node, ',', tg.next_chain+tg.count_cross_chain

#    start_time = time.time()
#    tg2 = Timegraph() 
#    tg2 = create_timegraph_from_weight_sorted_relations(tg.final_relations, tg2) 
#    end_time = time.time()

#    if debug >= 0: 
#        print 'time, count_relation, nodes, edges'
#    if debug >= 0: 
#        print end_time-start_time, ',', tg.count_relation, ',', tg.count_node, ',', tg.next_chain+tg.count_cross_chain
