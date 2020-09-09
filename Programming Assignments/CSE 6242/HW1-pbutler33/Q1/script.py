import http.client
import json
import time
import timeit
import sys
import collections
from pygexf.gexf import *
import pprint
import requests

from urllib.request import Request, urlopen
from collections import defaultdict


#
# implement your data retrieval code here
#
key = 'key %s' %(sys.argv[1])

req = Request('https://rebrickable.com/api/v3/lego/sets/?page=1&page_size=350&min_parts=1100&ordering=-num_parts',
              headers= {'Accept':'application/json', 'Authorization': key})
dat = urlopen(req).read()
lego = json.loads(dat)
lego = lego['results']

sets_data = {} #Will return data of sets as a dictionary with set names as keys, each containing info on more used parts
for i in range(len(lego)): # range(len(lego))
    time.sleep(1.0)
    url = 'https://rebrickable.com/api/v3/lego/sets/%s/parts/?page=1&page_size=1000&ordering=-quantity' %(lego[i]['set_num'])
    request = Request(url,
                      headers = {'Accept':'application/json', 'Authorization': key})
    data = urlopen(request).read()
    sets = json.loads(data)
    sets = sets['results'] ## Got list of parts for set i in lego
    sorted_sets = sorted(sets, key=lambda x: x['quantity'], reverse=True) #sorts set of parts by decreasing frequency
    parts = sorted_sets[:20] # Gives final list of parts: first 20 parts from sorted_sets
    sets_data[lego[i]['set_num']] = {}
    for j in range(len(parts)): #range(len(parts))
        id = parts[j]['part']['part_num'] +'_'+ parts[j]['color']['rgb']
        sets_data[lego[i]['set_num']][id] = {
                'color':parts[j]['color']['rgb'],
                'quantity':parts[j]['quantity'],
                'name':parts[j]['part']['name'],
                'number':parts[j]['part']['part_num']
                } 

##make the graph
gexf = Gexf('Peter Butler', 'Lego Sets & Parts Graph')
graph = gexf.addGraph(type = 'undirected', mode = 'static', label = 'lego_graph')
idType = graph.addNodeAttribute("Type", defaultValue="", type="string")
for i in lego: # Add each as a node to the graph
    n =  graph.addNode(id=i['set_num'], label=i['name'], r='0', g='0', b='0')
    n.addAttribute(idType, 'set')
    for j in sets_data[i['set_num']]:
        rgb = sets_data[i['set_num']][j]['color']
        if graph.nodeExists(j):
            graph.addEdge(id = i['set_num'] + '_' + j,
                          source = i['set_num'],
                          target = j,
                          weight = sets_data[i['set_num']][j]['quantity'])
        else:
            n = graph.addNode(id = j,
                          label = sets_data[i['set_num']][j]['name'],
                          r = str(int(rgb[:2], 16)),
                          g = str(int(rgb[2:4], 16)),
                          b = str(int(rgb[4:6], 16))
                          )
            n.addAttribute(idType, 'part')
            graph.addEdge(id = i['set_num'] + '_' + j,
                          source = i['set_num'],
                          target = j,
                          weight = sets_data[i['set_num']][j]['quantity'])
            

        
        
        

out_file = open('bricks_graph.gexf', 'wb')
gexf.write(out_file)
out_file.close()

# complete auto grader functions for Q1.1.b,d
def min_parts():
    """
    Returns an integer value
    """
    # you must replace this with your own value
    return 1100

def lego_sets():
    """
    return a list of lego sets.
    this may be a list of any type of values
    but each value should represent one set

    e.g.,
    biggest_lego_sets = lego_sets()
    print(len(biggest_lego_sets))
    > 280
    e.g., len(my_sets)
    """
    # you must replace this line and return your own list
    return lego

def gexf_graph():
    """
    return the completed Gexf graph object
    """
    # you must replace these lines and supply your own graph
    return graph


# complete auto-grader functions for Q1.2.d

def avg_node_degree():
    """
    hardcode and return the average node degree
    (run the function called “Average Degree”) within Gephi
    """
    # you must replace this value with the avg node degree
    return 5.541

def graph_diameter():
    """
    hardcode and return the diameter of the graph
    (run the function called “Network Diameter”) within Gephi
    """
    # you must replace this value with the graph diameter
    return 8

def avg_path_length():
    """
    hardcode and return the average path length
    (run the function called “Avg. Path Length”) within Gephi
    :return:
    """
    # you must replace this value with the avg path length
    return 4.408244394955358