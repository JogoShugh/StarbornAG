#!/bin/sh

echo "Create beds..."
./bedslocal.sh
echo "Water one bed, expect all 'null' ->"
echo ""
./waterlocal.sh
echo "Just after water, expect all 'null' ->"
echo ""
#./getlocal.sh
curl -N http://localhost:8080/api/beds/2fbda883-d49d-4067-8e16-2b04cc523111/events
#echo "After a bit, expect all 'watered'"
#echo ""
#sleep 2
#./getlocal.sh


