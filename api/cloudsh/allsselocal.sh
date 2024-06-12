#!/bin/sh

echo "Create beds..."
./bedslocal.sh
echo "Water one bed, expect all 'null' ->"
echo ""
./waterlocal.sh
echo "Just after with SSE ->"
echo ""
curl -N https://starbornag-vevkduzweq-uc.a.run.app/api/beds/2fbda883-d49d-4067-8e16-2b04cc523111/events


