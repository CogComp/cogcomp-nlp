#!/usr/bin/python
import matplotlib.pyplot as plt

# This plots results according to training size. Used to make that cool figure in the paper (but not used)

x = []
y = []

with open("results-Arabic.txt") as f:
    for line in f:
        if "#" in line: continue
        sline = line.split()
        x.append(sline[0])
        y.append(sline[1])

x2 = []
y2 = []
with open("results-hindi.txt") as f:
    for line in f:
        if "#" in line: continue
        sline = line.split()
        x2.append(sline[0])
        y2.append(sline[1])

x3 = []
y3 = []
with open("results-hebrew.txt") as f:
    for line in f:
        if "#" in line: continue
        sline = line.split()
        x3.append(sline[0])
        y3.append(sline[1])

x4 = []
y4 = []
with open("results-korean.txt") as f:
    for line in f:
        if "#" in line: continue
        sline = line.split()
        x4.append(sline[0])
        y4.append(sline[1])

x5 = []
y5 = []
with open("results-Persian.txt") as f:
    for line in f:
        if "#" in line: continue
        sline = line.split()
        x5.append(sline[0])
        y5.append(sline[1])


plt.plot(x,y, label="Arabic", marker="o")
#plt.plot(x2, y2, label="Hindi", marker="1")
plt.plot(x3, y3, label="Hebrew", marker="s")
plt.plot(x4, y4, label="Korean", marker="*")
plt.plot(x5, y5, label="Persian", marker="v")
plt.ylabel('MRR')
plt.xlabel('# Training Pairs')
plt.title("Effect of training size on MRR")
plt.legend(loc=4)
plt.savefig("transliteration-results.pdf",format='pdf')
plt.show()

