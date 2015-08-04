(ns app.player)


(defprotocol Player
  (step-fwd [this])
  (step-rnd [this])
  (animation [this])
  (render [this]))

