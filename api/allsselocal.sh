#!/bin/sh

echo "Create beds..."
./bedslocal.sh
echo "Water one bed, expect all 'null' ->"
echo ""
./waterlocal.sh
echo "Just after water, expect all 'null' ->"
echo ""
#./getlocal.sh
./listen.sh
#echo "After a bit, expect all 'watered'"
#echo ""
#sleep 2
#./getlocal.sh
