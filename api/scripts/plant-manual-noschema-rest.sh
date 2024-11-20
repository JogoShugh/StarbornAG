curl https://api.openai.com/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Accept: application/x-ndjson" \
  -H "Authorization: Bearer $OPENAPI" \
  -d @plant-manual-rest-noschema-request.json
