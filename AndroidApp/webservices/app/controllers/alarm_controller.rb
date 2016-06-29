class AlarmController < ApplicationController
  def status
    respond_to do |format|
      format.json { render json: { activated: true, ringing: false } }
    end
  end
end
