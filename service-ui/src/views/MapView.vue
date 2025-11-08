<template>
    <div class="map-container">
      <iframe
        src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3241.854048307187!2d139.6983875152596!3d35.69382598019347!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x60188cd4fdd65e11%3A0xf78bb7ebdbca351f!2sPrometric%20Shinjuku%20Ekimae%20Test%20Center!5e0!3m2!1sen!2sjp!4v1683476924585!5m2!1sen!2sjp"
        width="300"
        height="200"
        style="border:0;"
        allowfullscreen=""
        loading="lazy"
        referrerpolicy="no-referrer-when-downgrade"
      ></iframe>
    </div>
  </template>
  
  <script>
  export default {
    name: 'GoogleMapEmbed',
  };
  </script>
  
  <style scoped>
  .map-container {
    width: 100%;
    max-width: 320px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  }
  </style>
  