#!/usr/bin/python
import codecs
from collections import defaultdict
from Levenshtein import distance
import os

def readfile(fname):
    """
    This reads the outgen file and returns two lists

    Gold answers:
    [(queryname : goldtransliteration), ...]

    Ranked predictions:
    [(queryname, [(rank1name, score), (rank2name, score), ...]), (queryname, [...]), ...]
    """
    ans = []
    pred = []
    currpred = []
    currquery = None
    with codecs.open(fname, "r", "utf8") as f:
        for line in f:
            line = line.strip()
            if len(line) == 0:
                continue
            sline = line.split()
            if sline[0] == "SourceWord:":

                # stores the results from last time.
                if currquery != None:
                    pred.append((currquery, currpred))
                
                currquery = sline[1]
                currpred = []
            elif sline[0] == "TransliteratedWords:":
                currgold = sline[1]

                ans.append((currquery,currgold))


            else:
                prediction = sline[0].strip("*,")
                score = float(sline[1].strip("*,"))
                currpred.append((prediction,score))

    pred.append((currquery,currpred))
    
    return ans,pred

            
def getScores(ans,pred):
    """
    This can get the MRR,ACC,F1 of the returned result.
    """
    total = 0
    correct = 0
    mrrcorrect = 0
    totalf1 = 0

    for a,p in zip(ans,pred):

        ranks = p[1]
        gold = a[1]
        
        if len(ranks) ==  0:
            continue
        
        toprank = ranks[0]

        # get the ACC
        if gold == toprank[0]:
            correct += 1

        # get the F1
        d = distance(toprank[0], gold)
        lcs = (len(toprank[0]) + len(gold) - d)/2.
        R = lcs / len(gold)
        P = lcs / len(toprank[0])
        F1 = 2 * R * P / (R + P)
        totalf1 += F1
            
        # get the MRR
        for j,r in enumerate(ranks):
            name = r[0]
            score = r[1]

            if gold == name:
                mrrcorrect += 1 / float(j+1)
                break

        
    MRR = mrrcorrect / float(len(pred))
    ACC = correct / float(len(pred))
    F1 = totalf1 / float(len(pred))
    return MRR,ACC,F1



def writeAnsFile(ans,ranking):

    with open("../out-gen-Combined.txt", "w") as out:
        for a,p in zip(ans,ranking):
            query = p[0]
            gold = a[1]
            ranking = p[1]
            out.write("SourceWord: {0}\n".format(query.encode("utf8")))
            out.write("TransliteratedWords: {0}\n".format(gold.encode("utf8")))
            for pr in ranking:
                name = pr[0]
                score = pr[1]
                out.write("{0}, {1}\n".format(name.encode("utf8"), score))
            out.write("\n\n")
            

def combine(*fnames):
    """
    This combines the different language files into a single version, hopefully better than all the rest.
    """

    # this will hold all predictions from each file
    predictions = []

    ans = None
    for f in fnames:
        ans,pred = readfile(f)
        print f, getScores(ans,pred)
        predictions.append(pred)

    reranked = []

    # ans is identical in each file, so just use last one
    for i,q in enumerate(ans):
        query = q[0]
        gold = q[1]

        dct = defaultdict(list)

        for pred in predictions:        
            qname,ranking = pred[i]
            for j,p in enumerate(ranking):
                name = p[0]
                score = p[1]

                dct[name].append(score) # alternatively, append j
                #dct[name].append(1./(j+1)) # alternatively, append j
                
        newranking = []
        for n in dct:
            # this is a list of scores given for candidates for name n
            votes = dct[n]

            # different strategies are represented here.
            # get the average score
            avg = sum(votes) / float(len(votes))

            # get the total score
            sumvotes = sum(votes)

            numvotes = len(votes)
            
            val = sumvotes
            newranking.append((n,val))
            
        newranking = sorted(newranking, key=lambda p: p[1], reverse=True)
        
        reranked.append((query, newranking))
    

    print "overall: ", getScores(ans, reranked)
    writeAnsFile(ans,reranked)


        
        
                

        
    

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="Combine two result files from transliteration")

    parser.add_argument("files",help="Filenames of output files. Typically out-gen-Lang.txt", nargs="+")

    args = parser.parse_args()

    print args.files
    
    combine(*args.files)
