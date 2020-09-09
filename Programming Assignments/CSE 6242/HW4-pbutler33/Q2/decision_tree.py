from util import entropy, information_gain, partition_classes
import numpy as np
import ast

class DecisionTree(object):
    def __init__(self):
        # Initializing the tree as an empty dictionary or list, as preferred
        #self.tree = []
        self.tree = {}


    def learn(self, X, y):
        # TODO: Train the decision tree (self.tree) using the the sample X and labels y
        # You will have to make use of the functions in utils.py to train the tree

        # One possible way of implementing the tree:
        #    Each node in self.tree could be in the form of a dictionary:
        #       https://docs.python.org/2/library/stdtypes.html#mapping-types-dict
        #    For example, a non-leaf node with two children can have a 'left' key and  a
        #    'right' key. You can add more keys which might help in classification
        #    (eg. split attribute and split value)
        if entropy(y) == 0:
            self.tree['type'] = 'leaf'
            self.tree['val'] = y[0]
        else:
            self.tree['type'] = 'branch'
            best_attr = np.argmax([information_gain(y, partition_classes(X, y, i, np.mean(X[:,i]))[2:]) for i in range(X.shape[1])])
            best_value = np.mean(X[:,best_attr])
            X_left, X_right, y_left, y_right = partition_classes(X, y, best_attr, best_value)
            self.tree['left'] = DecisionTree()
            self.tree['right'] = DecisionTree()
            self.tree['left'].learn(X_left, y_left)
            self.tree['right'].learn(X_right, y_right)
            self.tree['split_attr'] = best_attr
            self.tree['split_val'] = best_value




    def classify(self, record):
        # TODO: classify the record using self.tree and return the predicted label
        if self.tree['type'] == 'leaf':
            return self.tree['val']
        else:
            if isinstance(record[self.tree['split_attr']], int) or isinstance(record[self.tree['split_attr']], float):
                if record[self.tree['split_attr']] <= self.tree['split_val']:
                    return self.tree['left'].classify(record)
                else:
                    return self.tree['right'].classify(record)
            else:
                if record[self.tree['split_attr']] == self.tree['split_val']:
                    return self.tree['left'].classify(record)
                else:
                    return self.tree['right'].classify(record)
