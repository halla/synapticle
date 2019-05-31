(ns app.player.player)

;; Player protocol 
;;
;; * Implement Player protocol
;; * Register player at handler
;; * Add ui option



(defprotocol Player
  (step-fwd [this screen channels])
  (step-rnd [this screen channels])
  (animation [this screen])
  (render [this screen]))

