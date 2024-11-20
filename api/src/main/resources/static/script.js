

document.addEventListener('DOMContentLoaded', function() {

    function plantTypeToIcon(plantType) {
        switch (plantType.toLowerCase()) {
            case "tomato": return "🍅";
            case "eggplant": return "🍆";
            case "potato": return "🥔";
            case "carrot": return "🥕";
            case "corn": return "🌽";
            case "hot pepper": return "🌶️";
            case "bell pepper": return "🫑";
            case "cucumber": return "🥒";
            case "broccoli": return "🥦";
            case "garlic": return "🧄";
            case "onion": return "🧅";
            case "lettuce": return "🥬";
            case "sweet potato": return "🍠";
            case "chili pepper": return "🌶";
            case "mushroom": return "🍄";
            case "peanuts": return "🥜";
            case "beans": return "🫘";
            case "chestnut": return "🌰";
            case "ginger root": return "🫚";
            case "shallot": return "🫛";
            case "herb": return "🌿";
            default: return ""; // Default case (for unknown plant types)
        }
    }

  const iconMap = {
    'mulched': '🪵',
    'watered': '💧',
    'fertilized': '🌿'
  };

//  htmx.on('htmx:sseBeforeMessage', function(evt) {
//      console.log("sseBeforeMessage hx-target initial value:")
//      console.log(evt)
//      console.log(evt.target.getAttribute("hx-target"))
//      const data = JSON.parse(evt.detail.data)
//      const type = data.type
//      evt.detail.data = "T" // Can I change the data?
//      evt.target.setAttribute("hx-target", "#events-" + data.bedCellId)
//  })

  /*
      if (type === 'planted') {
        const plant = plantTypeToIcon(data.plantType);
        const plantContainer = targetElement.closest('.grid-item').querySelector('.plant-icon');
        evt.detail.data = plant;
        evt.target = plantContainer;
        evt.redirectTarget = plantContainer;
      } else {
        const icon = iconMap[data.type];
        evt.detail.data = icon;
        evt.target = targetElement;
        evt.redirectTarget = targetElement;
        evt.detail.elt.setAttribute("hx-target", "#events-" + data.bedCellId);
      }

  */

  htmx.on('htmx:sseError', function(e) {
    console.log('In error event listener:');
    console.log(e);
  });

});