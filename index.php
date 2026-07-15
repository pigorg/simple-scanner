<!DOCTYPE html>
<html lang="it">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>IstoreLab</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Share+Tech+Mono&display=swap');

  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  html, body {
    height: 100%;
  }

  body {
    background: #000;
    color: #00ff41;
    font-family: 'Share Tech Mono', 'Consolas', monospace;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    overflow: hidden;
    position: relative;
  }

  body::before {
    content: "";
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: repeating-linear-gradient(
      0deg,
      rgba(0, 255, 65, 0.04),
      rgba(0, 255, 65, 0.04) 1px,
      transparent 1px,
      transparent 2px
    );
    pointer-events: none;
    z-index: 2;
  }

  .terminal {
    position: relative;
    z-index: 1;
    padding: 40px;
  }

  img.logo {
    max-width: 140px;
    width: 100%;
    height: auto;
    filter: drop-shadow(0 0 10px #00ff41) drop-shadow(0 0 20px #00ff41);
    margin-bottom: 24px;
  }

  .brand {
    font-weight: bold;
    font-size: 1.4rem;
    letter-spacing: 2px;
    text-shadow: 0 0 5px #00ff41, 0 0 15px #00ff41, 0 0 30px #00ff41;
    margin-bottom: 16px;
  }

  .brand::after {
    content: "_";
    animation: blink 1s steps(1) infinite;
  }

  @keyframes blink {
    50% { opacity: 0; }
  }

  a {
    color: #00ff41;
    text-decoration: none;
    font-size: 1rem;
    letter-spacing: 1px;
    border-bottom: 1px dashed #00ff41;
    text-shadow: 0 0 5px #00ff41, 0 0 10px #00ff41;
    transition: color 0.2s, text-shadow 0.2s;
  }

  a:hover {
    color: #b6ffb6;
    text-shadow: 0 0 8px #fff, 0 0 20px #00ff41;
  }
</style>
</head>
<body>
  <div class="terminal">
    <img class="logo" src="logo.png" alt="IstoreLab logo">
    <div class="brand">IstoreLab | Indie Dev | RSM | San Marino</div>
    <a href="mailto:informazioni@istore-lab.com">informazioni@istore-lab.com</a>
  </div>
</body>
</html>
