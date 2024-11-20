<!DOCTYPE html>
<html>
<head>
  <title>JSON Forms CDN Example</title>
  <!-- Load JSON Forms core library -->
  <script src="https://cdn.jsdelivr.net/npm/@jsonforms/core@latest/dist/jsonforms-core.umd.js"></script>
  <!-- Load JSON Forms React library -->
  <script src="https://cdn.jsdelivr.net/npm/@jsonforms/react@latest/dist/jsonforms-react.umd.js"></script>
  <!-- Load JSON Forms Material Renderers -->
  <script src="https://cdn.jsdelivr.net/npm/@jsonforms/material-renderers@latest/dist/jsonforms-material-renderers.umd.js"></script>
  <!-- Load React and ReactDOM -->
  <script src="https://unpkg.com/react@17/umd/react.production.min.js" crossorigin></script>
  <script src="https://unpkg.com/react-dom@17/umd/react-dom.production.min.js" crossorigin></script>
  <!-- Load Material UI dependencies -->
  <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500">
  <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mui/material@latest/dist/material-ui.css">
  <script src="https://cdn.jsdelivr.net/npm/@emotion/react@latest/dist/emotion-react.umd.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/@emotion/styled@latest/dist/emotion-styled.umd.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/@mui/material@latest/umd/material-ui.development.js"></script>
</head>
<body>
  <div id="app"></div> 
  <script>
    const schema = {
      "type": "object",
      "title": "Bed",
      "properties": {
        "bedId": {
          "title": "Bed ID",
          "type": "string",
          "format": "uuid"
        },
        "cellBlockSize": {
          "type": "integer"
        },
        "dimensions": {
          "type": "object",
          "properties": {
            "columns": {
              "type": "integer"
            },
            "height": {
              "type": "integer"
            },
            "rows": {
              "type": "integer"
            }
          }
        },
        "name": {
          "type": "string"
        }
      }
    };

    const data = {
      "bedId": "2fbda883-d49d-4067-8e16-2b04cc523111",
      "name": "Venus",
      "dimensions": {
        "columns": 8,
        "height": 1,
        "rows": 4
      }
    };

    const uischema = {
      "type": "Group",
      "elements": [
        {
          "type": "Control",
          "scope": "#/properties/bedId"
        },
        {
          "type": "Control",
          "scope": "#/properties/name"
        },
        {
          "type": "Control",
          "scope": "#/properties/dimensions/properties/columns"
        },
        {
          "type": "Control",
          "scope": "#/properties/dimensions/properties/height"
        },
        {
          "type": "Control",
          "scope": "#/properties/dimensions/properties/rows"
        }
      ]
    };

    const app = document.getElementById('app'); 

    // Render JSON Forms using React
    const render = () => {
      ReactDOM.render(
        React.createElement(JsonFormsReactProvider, {
          schema,
          uischema,
          data,
          renderers: JsonFormsMaterialRenderers.materialRenderers,
        }),
        app
      );
    };

    // Wait for the JSON Forms library to load (adjust the timeout if needed)
    setTimeout(render, 500);
  </script> 
</body>
</html>
