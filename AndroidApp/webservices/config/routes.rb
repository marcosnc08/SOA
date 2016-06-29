Rails.application.routes.draw do
  get '/alarm/status', to: 'alarm#status'
end
