#!/bin/sh
clientId=1AF7867B-CFC0-48C4-A7A9-0BAFBCDC5569

curl -vvv -H "Content-Type: application/json" \
-H "Accept: application/json" \
-N "$API/algo/insertionSort/$clientId?delayMs=150" -d "[77,3,9,0,-10,12]"
