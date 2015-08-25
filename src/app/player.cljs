(ns app.player)


(defprotocol Player
  (step-fwd [this screen channels])
  (step-rnd [this screen channels])
  (animation [this screen])
  (render [this screen]))

