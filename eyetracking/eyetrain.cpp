#include <iostream>
#include <fstream>
#include <string>
#include <unistd.h>
#include "doublefann.h"
#include "fann_cpp.h"

const unsigned int eye_size = 36 * 26 / 4;

static int print_callback(FANN::neural_net &net, FANN::training_data &train,
    unsigned int max_epochs, unsigned int epochs_between_reports,
    float desired_error, unsigned int epochs, void *user_data)
{
  return 0;
}

static void create_nn(FANN::neural_net& net) {
  const float learning_rate = 0.7f;
  const unsigned int num_layers = 3;
  const unsigned int num_input = eye_size;
  const unsigned int num_hidden = 32;
  const unsigned int num_output = 2;

  net.create_standard(num_layers, num_input, num_hidden, num_output);

  net.set_learning_rate(learning_rate);

  net.set_activation_steepness_hidden(1.0);
  net.set_activation_steepness_output(1.0);

  net.set_activation_function_hidden(FANN::SIGMOID_SYMMETRIC_STEPWISE);
  net.set_activation_function_output(FANN::SIGMOID_SYMMETRIC_STEPWISE);
}

static void net_train(FANN::neural_net& net, FANN::training_data& data, char* outf) {
  const float desired_error = 0.001f;
  const unsigned int max_iterations = 250;// 300000;
  const unsigned int iterations_between_reports = 50; // 1000

  net.init_weights(data);
  net.set_callback(print_callback, NULL);
  net.train_on_data(data, max_iterations, iterations_between_reports, desired_error);

  net.save(std::string(outf));
}

int main(int argc, char** argv) {
  FANN::neural_net net;
  create_nn(net);

  if (argc != 3) {
    std::cerr << "Usage: " << argv[0] << " infile outfile" << std::endl;
    return 1;
  }

  FANN::training_data data;
  data.read_train_from_file(std::string(argv[1]));
  net_train(net, data, argv[2]);

  return 0;
}
